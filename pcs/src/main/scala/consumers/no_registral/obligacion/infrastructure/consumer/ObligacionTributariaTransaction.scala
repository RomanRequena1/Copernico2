package consumers.no_registral.obligacion.infrastructure.consumer
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.entities.ObligacionCommands._
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ObligacionCommands, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

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

    val isNotDeuda: List[Boolean] = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES map {
      d =>
        d.RULE_NUMBER match {
          case "-1" => true
          case _ => false
        }
    }

    val isCancelada = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map {
      d =>
        d.RULE_NUMBER match {
          case "-2" => true
          case _ => false
        }
    }


    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))

    val command: ObligacionCommands =
      if (isCancelada.head)
        ObligacionCommands.ObligacionRemove(
          deliveryId = obligacion.EV_ID,
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          registro = obligacion,
          cuota = obligacion.BOB_CUOTA)

      else if (isNotDeuda.head)
        ObligacionCommands.ObligacionRemove(
          deliveryId = obligacion.EV_ID,
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          registro = obligacion,
          cuota = None)

      else
        ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = obligacion,
          detallesObligacion = Seq[DetallesObligacion],
          isAdheridoDebito = isAdheridoDebito)


    actorRef.ask[Response.SuccessProcessing](command)
  }

}
