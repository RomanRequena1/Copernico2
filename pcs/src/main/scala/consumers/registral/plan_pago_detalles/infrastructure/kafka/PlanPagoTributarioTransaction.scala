package consumers.registral.plan_pago_detalles.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoCommands
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoExternalDto.PlanPagoTri
import consumers.registral.plan_pago_detalles.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago_detalles.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future

case class PlanPagoTributarioTransaction(actor: PlanPagoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[PlanPagoTri](monitoring) {
  def topic = "DGR-COP-PLANES-TRI-OBN"
  def topicRetry = "DGR-COP-PLANES-TRI-OBN_retry"
  def topicError = "DGR-COP-PLANES-TRI-OBN_error"

  def processInput(input: String): Either[Throwable, PlanPagoTri] = {
    decode[PlanPagoTri](input)
  }

  override def processMessage(registro: PlanPagoTri): Future[Response.SuccessProcessing] = {

    val command: PlanPagoCommands =
    if(registro.RULE_NUMBER.contains("-1")){
      PlanPagoCommands.PlanPagoRemoveFromDto(
        deliveryId = registro.EV_ID,
        planPagoId = registro.BPL_IDENTIFICADOR,
        tipoObjeto = registro.BPD_SOJ_TIPO_OBJETO,
        objetoId = registro.BPD_SOJ_IDENTIFICADOR,
        obligacionId = registro.BPD_OBN_ID,
        registro = registro
      )
    }
    else {
       PlanPagoCommands.PlanPagoUpdateFromDto(
        deliveryId = registro.EV_ID,
        planPagoId = registro.BPL_IDENTIFICADOR,
        tipoObjeto = registro.BPD_SOJ_TIPO_OBJETO,
        objetoId = registro.BPD_SOJ_IDENTIFICADOR,
        obligacionId = registro.BPD_OBN_ID,
        registro = registro
      )
    }
    actor.ask(command)
  }
}
