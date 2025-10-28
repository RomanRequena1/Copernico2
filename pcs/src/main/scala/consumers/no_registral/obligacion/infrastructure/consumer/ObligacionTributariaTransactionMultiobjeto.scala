package consumers.no_registral.obligacion.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.dmn.DMNTreintaPorciento
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{ObligacionRemove, ObligacionUpdateFromDto}
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, DetallesObligacionCaracteristicas, DetallesSupresiones, ListDetallesObligaciones, ObligacionCommands, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import monitoring.Monitoring
import org.camunda.dmn.DmnEngine
import org.camunda.dmn.parser.ParsedDmn
import org.slf4j.LoggerFactory
import scalaz.\/

import java.io.FileInputStream
import scala.concurrent.Future
import scala.util.Try

case class ObligacionTributariaTransactionMultiobjeto(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesTri](monitoring) {
  private val log = LoggerFactory.getLogger(this.getClass)

  /** Handles the deserialization of detalles de obligaciones tributarias */
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  def topic = "DGR-COP-OBLIGACIONES-TRI-M"
  def topicRetry = "DGR-COP-OBLIGACIONES-TRI_retry"
  def topicError = "DGR-COP-OBLIGACIONES-TRI_error"

  def processInput(input: String): Either[Throwable, ObligacionesTri] = {

    decode[ObligacionesTri](input)
  }

  def processMessage(obligacion: ObligacionesTri): Future[Response.SuccessProcessing] = {
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

    if (obligacion.BOB_SUJ_IDENTIFICADOR == "" || obligacion.BOB_SOJ_IDENTIFICADOR == "" || obligacion.BOB_SOJ_TIPO_OBJETO == "" || obligacion.BOB_OBN_ID == "") {
      Future.failed(new IllegalArgumentException("Campos obligatorios vacíos, operación omitida"))
    } else {
      val command: ObligacionCommands =
        if (isCancelada(obligacion.BOB_OTROS_ATRIBUTOS).head) {
          ObligacionCommands.ObligacionRemove(
            deliveryId = obligacion.EV_ID,
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            registro = obligacion,
            cuota = obligacion.BOB_CUOTA
          )
        } else if (isNotDeuda(obligacion.BOB_OTROS_ATRIBUTOS).head) {
          ObligacionCommands.ObligacionRemove(
            deliveryId = obligacion.EV_ID,
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            registro = obligacion,
            cuota = obligacion.BOB_CUOTA
          )
        } else {
          val dmn = isTreintaPorciento(obligacion)
          ObligacionUpdateFromDto(
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            deliveryId = obligacion.EV_ID,
            registro = dmn._1,
            detallesObligacion = detallesObligacion,
            detallesCaracteristicas = detallesObligacionCaracteristicas,
            detallesSupresiones = detallesSupresiones,
            isAdheridoDebito = isAdheridoDebito,
            cuota = obligacion.BOB_CUOTA,
            resultDmn = Some(dmn._2.toString)
          )
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
        (newO, (numero, descripcion))  // ← Retornar tupla tipada
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
        (newO, (numero, descripcion))  // ← Retornar tupla tipada
      }
      case None => (obn, (-999, "Error en DMN"))  // ← Retornar tupla tipada
    }
  }
}
