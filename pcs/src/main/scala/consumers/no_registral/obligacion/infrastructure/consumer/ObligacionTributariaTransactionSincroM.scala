package consumers.no_registral.obligacion.infrastructure.consumer

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.dmn.DMNTreintaPorciento
import consumers.no_registral.obligacion.application.entities.ObligacionCommands._
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, DetallesObligacionCaracteristicas, DetallesSupresiones, ListDetallesObligaciones, ObligacionCommands, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import consumers.no_registral.obligacion.infrastructure.sorter.ObligacionCommandRouter
import design_principles.actor_model.Response
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import monitoring.Monitoring

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Either

case class ObligacionTributariaTransactionSincroM(actorRef: ActorRef, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements,
  system: ActorSystem
) extends ActorTransaction[ObligacionesTri](monitoring) {

  implicit val timeout: Timeout = Timeout(30.seconds)
  implicit val ec: ExecutionContext = actorTransactionRequirements.executionContext

  val sorterEnabled: String = Option(System.getenv("BETTER_SORTER_OBLIGACION_TRI")).getOrElse("OFF")
//  private val commandRouter = ObligacionCommandRouter.getOrCreate(system, actorRef)

  def topic = "DGR-COP-OBLIGACIONES-TRI-M-SINCRO"
  def topicRetry = "DGR-COP-OBLIGACIONES-TRI_retry"
  def topicError = "DGR-COP-OBLIGACIONES-TRI_error"

  def processInput(input: String): Either[Throwable, ObligacionesTri] = {
    decode[ObligacionesTri](input)
  }

  def processMessage(obligacion: ObligacionesTri): Future[Response.SuccessProcessing] = {
    // ✅ EVALUAR DMN SIEMPRE, ANTES DE CUALQUIER DECISIÓN
    val dmn = isTreintaPorciento(obligacion)
    val dmnResultTuple = dmn._2
    val dmnNumero = dmnResultTuple._1
    val dmnDescripcion = dmnResultTuple._2

    val isNotDeuda: Option[ListDetallesObligaciones] => List[Boolean] = {
      case Some(d) =>
        d.BOB_DETALLES map { d =>
          d.RULE_NUMBER.contains("-1")
        }
      case None => List(false)
    }
    val isCancelada: Option[ListDetallesObligaciones] => List[Boolean] = {
      case Some(d) =>
        d.BOB_DETALLES map { d =>
          d.RULE_NUMBER.contains("-2")
        }
      case None => List(false)
    }

    val detallesObligacion: Seq[DetallesObligacion] = obligacion.BOB_OTROS_ATRIBUTOS match {
      case Some(r) => r.BOB_DETALLES
      case None => null
    }
    val detallesObligacionCaracteristicas: Seq[DetallesObligacionCaracteristicas] = obligacion.BOB_CARACTERISTICAS match {
      case Some(r) => r.BOB_DETALLES_CARACTERISTICAS
      case None => null
    }
    val detallesSupresiones: Seq[DetallesSupresiones] = obligacion.BOB_SUPRESIONES match {
      case Some(r) => r.BOB_DETALLES_SUPRESIONES
      case None => null
    }

    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))

    if (obligacion.BOB_SUJ_IDENTIFICADOR == "" || obligacion.BOB_SOJ_IDENTIFICADOR == "" ||
      obligacion.BOB_SOJ_TIPO_OBJETO == "" || obligacion.BOB_OBN_ID == "") {
      Future.failed(new IllegalArgumentException("Campos obligatorios vacíos, operación omitida"))
    } else {
      val command: ObligacionCommands =
        if (isCancelada(obligacion.BOB_OTROS_ATRIBUTOS).head) {
          // ✅ CASO CANCELADA: Usar el registro CON DMN evaluado
          ObligacionCommands.ObligacionRemove(
            deliveryId = obligacion.EV_ID,
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            registro = dmn._1,  // ✅ Ya tiene el DMN evaluado
            cuota = obligacion.BOB_CUOTA,
            resultDmn = Some(s"($dmnNumero,$dmnDescripcion)")  // ✅ Pasar resultado del DMN
          )
        } else if (isNotDeuda(obligacion.BOB_OTROS_ATRIBUTOS).head) {
          // ✅ CASO NO DEUDA (RULE_NUMBER = -1): Usar el registro CON DMN evaluado
          ObligacionCommands.ObligacionRemove(
            deliveryId = obligacion.EV_ID,
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            registro = dmn._1,  // ✅ Ya tiene el DMN evaluado
            cuota = obligacion.BOB_CUOTA,
            resultDmn = Some(s"($dmnNumero,$dmnDescripcion)")  // ✅ Pasar resultado del DMN
          )
        } else {
          // ✅ CASO NORMAL: Actualizar obligación
          if (dmnNumero == 1) {
            val obligacionNoDeuda = obligacion.copy(
              BOB_OTROS_ATRIBUTOS = obligacion.BOB_OTROS_ATRIBUTOS.map { detalles =>
                detalles.copy(
                  BOB_DETALLES = detalles.BOB_DETALLES.map { d =>
                    d.copy(
                      tiene30Obligaciones = Some(true),
                      BAND_BATCH = Some(false),
                      EV_ID = Some(obligacion.EV_ID),
                      SOJ_ID_EXTERNO = obligacion.SOJ_ID_EXTERNO,
                      dmnNumero = Some(dmnNumero),
                      dmnDescripcion = Some(dmnDescripcion)
                    )
                  }
                )
              }
            )

            ObligacionUpdateFromDto(
              deliveryId = obligacion.EV_ID,
              sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
              objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
              tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
              obligacionId = obligacion.BOB_OBN_ID,
              registro = obligacionNoDeuda,
              detallesObligacion = detallesObligacion,
              detallesCaracteristicas = detallesObligacionCaracteristicas,
              idExterno = obligacion.SOJ_ID_EXTERNO,
              detallesSupresiones = detallesSupresiones,
              isAdheridoDebito = isAdheridoDebito,
              cuota = obligacion.BOB_CUOTA,
              resultDmn = Some(s"($dmnNumero,$dmnDescripcion)")
            )
          } else {
            ObligacionUpdateFromDto(
              deliveryId = obligacion.EV_ID,
              sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
              objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
              tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
              obligacionId = obligacion.BOB_OBN_ID,
              registro = dmn._1,
              detallesObligacion = detallesObligacion,
              detallesCaracteristicas = detallesObligacionCaracteristicas,
              idExterno = obligacion.SOJ_ID_EXTERNO,
              detallesSupresiones = detallesSupresiones,
              isAdheridoDebito = isAdheridoDebito,
              cuota = obligacion.BOB_CUOTA,
              resultDmn = Some(s"($dmnNumero,$dmnDescripcion)")
            )
          }
        }

      actorRef.ask[Response.SuccessProcessing](command)
    }
  }

  private def isTreintaPorciento(obn: ObligacionesTri): (ObligacionesTri, (Int, String)) = {
    val dmnResult = DMNTreintaPorciento.dmn(obn)

    dmnResult match {
      case Some((numero, descripcion)) if numero.equals(1) => {
        val detalles: Option[List[DetallesObligacion]] = Some(
          obn.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map(m =>
            m.copy(
              tiene30Obligaciones = Some(true),
              BAND_BATCH = Some(false),
              EV_ID = Some(obn.EV_ID),
              SOJ_ID_EXTERNO = obn.SOJ_ID_EXTERNO,
              dmnNumero = Some(numero),
              dmnDescripcion = Some(descripcion)
            )
          )
        )
        val newDetails =
          decode[ListDetallesObligaciones](ListDetallesObligaciones(detalles.get).asJson.toString()).toOption.get
        val newO: ObligacionesTri = obn.copy(BOB_OTROS_ATRIBUTOS = Some(newDetails))
        (newO, (numero, descripcion))
      }
      case Some((numero, descripcion)) if !numero.equals(1) => {
        val detalles: Option[List[DetallesObligacion]] = Some(
          obn.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map(m =>
            m.copy(
              tiene30Obligaciones = Some(false),
              BAND_BATCH = Some(false),
              EV_ID = Some(obn.EV_ID),
              SOJ_ID_EXTERNO = obn.SOJ_ID_EXTERNO,
              dmnNumero = Some(numero),
              dmnDescripcion = Some(descripcion)
            )
          )
        )
        val newDetails =
          decode[ListDetallesObligaciones](ListDetallesObligaciones(detalles.get).asJson.toString()).toOption.get
        val newO: ObligacionesTri = obn.copy(BOB_OTROS_ATRIBUTOS = Some(newDetails))
        (newO, (numero, descripcion))
      }
      case None => (obn, (-999, "Error en DMN"))
    }
  }
}