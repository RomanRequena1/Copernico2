package consumers.registral.plan_pago_detalles.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoCommands
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoExternalDto.PlanPagoAnt
import consumers.registral.plan_pago_detalles.infrastructure.dependency_injection.PlanPagoActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import consumers.registral.plan_pago_detalles.infrastructure.json.json._
import monitoring.Monitoring
import io.circe.parser.decode
import scala.concurrent.Future

case class PlanPagoNoTributarioTransaction(actor: PlanPagoActor, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[PlanPagoAnt](monitoring) {

  def topic = "DGR-COP-PLANES-ANT-OBN"
  def topicRetry = "DGR-COP-PLANES-ANT-OBN_retry"
  def topicError = "DGR-COP-PLANES-ANT-OBN_error"

  def processInput(input: String): Either[Throwable, PlanPagoAnt] =
    decode[PlanPagoAnt](input)

  override def processMessage(registro: PlanPagoAnt): Future[Response.SuccessProcessing] = {
    val command = PlanPagoCommands.PlanPagoUpdateFromDto(
      deliveryId = registro.EV_ID,
      planPagoId = registro.BPL_IDENTIFICADOR,
      tipoObjeto = registro.BPD_SOJ_TIPO_OBJETO,
      objetoId = registro.BPD_SOJ_IDENTIFICADOR,
      obligacionId = registro.BPD_OBN_ID,
      registro = registro
    )
    actor.ask(command)
  }
}