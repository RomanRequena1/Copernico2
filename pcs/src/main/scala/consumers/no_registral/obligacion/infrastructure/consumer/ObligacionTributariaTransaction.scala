package consumers.no_registral.obligacion.infrastructure.consumer
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.dmn.DMNTreintaPorciento
import consumers.no_registral.obligacion.application.entities.ObligacionCommands._
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ListDetallesObligaciones, ObligacionCommands, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import monitoring.Monitoring
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class ObligacionTributariaTransaction(actorRef : ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesTri](monitoring) {
  def topic = "DGR-COP-OBLIGACIONES-TRI"

  def topicRetry = "DGR-COP-OBLIGACIONES-TRI_retry"

  def topicError = "DGR-COP-OBLIGACIONES-TRI_error"


  def processInput(input: String): Either[Throwable, ObligacionesTri] = {
    decode[ObligacionesTri](input)
  }

  def processMessage(obligacion: ObligacionesTri): Future[Response.SuccessProcessing] = {
    val isNotDeuda: Option[ListDetallesObligaciones] => List[Boolean] = {
      case Some(d) => d.BOB_DETALLES map {
        d => d.RULE_NUMBER.contains("-1")
      }
      case None => List(false)
    }
    val isCancelada: Option[ListDetallesObligaciones] => List[Boolean] = {
      case Some(d) => d.BOB_DETALLES map {
        d => d.RULE_NUMBER.contains("-2")
      }
      case None => List(false)
    }

    val detallesObligacion: Seq[DetallesObligacion] = obligacion.BOB_OTROS_ATRIBUTOS match {
      case Some(r) => r.BOB_DETALLES
      case None => null
      }

    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))

    val command: ObligacionCommands =
      if (isCancelada(obligacion.BOB_OTROS_ATRIBUTOS).head) {
        ObligacionCommands.ObligacionRemove(
          deliveryId = obligacion.EV_ID,
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          registro = obligacion,
          cuota = obligacion.BOB_CUOTA)
      }
      else if (isNotDeuda(obligacion.BOB_OTROS_ATRIBUTOS).head) {
        ObligacionCommands.ObligacionRemove(
          deliveryId = obligacion.EV_ID,
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          registro = obligacion,
          cuota = None)
      }
      else {
        ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = isTreintaPorciento(obligacion),
          detallesObligacion = detallesObligacion,
          isAdheridoDebito = isAdheridoDebito,
          cuota = obligacion.BOB_CUOTA)
      }
    actorRef.ask[Response.SuccessProcessing](command)
  }

  private def isTreintaPorciento(obn: ObligacionesTri) = {
    //todo set deuda30Obligacion en state
    Some(DMNTreintaPorciento.dmn(obn)) match {
      case f if f.get.equals(0) => { //case 0
        val detalles: Option[List[DetallesObligacion]] = Some(obn.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map(m => m.copy(BAND_30 = Some(true), BAND_BATCH = Some(false), EV_ID = Some(obn.EV_ID), SOJ_ID_EXTERNO = obn.SOJ_ID_EXTERNO)))
        val newDetails = decode[ListDetallesObligaciones](ListDetallesObligaciones(detalles.get).asJson.toString()).toOption.get
        val newO: ObligacionesTri = obn.copy(BOB_OTROS_ATRIBUTOS = Some(newDetails))
        println(detalles)
        println(newO)
        newO
      }
      case _ => {
        val detalles: Option[List[DetallesObligacion]] = Some(obn.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map(m => m.copy(BAND_30 = Some(false), BAND_BATCH = Some(false), EV_ID = Some(obn.EV_ID), SOJ_ID_EXTERNO = obn.SOJ_ID_EXTERNO)))
        val newDetails = decode[ListDetallesObligaciones](ListDetallesObligaciones(detalles.get).asJson.toString()).toOption.get
        val newO: ObligacionesTri = obn.copy(BOB_OTROS_ATRIBUTOS = Some(newDetails))
        println(newDetails)
        println(newO)
        newO
      }
    }
  }
}
