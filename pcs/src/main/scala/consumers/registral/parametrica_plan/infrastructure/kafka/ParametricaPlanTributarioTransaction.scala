package consumers.registral.parametrica_plan.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanCommands
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.ParametricaPlanTri
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import consumers.registral.parametrica_plan.infrastructure.json.ParametricaPlanImplicits._
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future

case class ParametricaPlanTributarioTransaction(actor: ParametricaPlanActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ParametricaPlanTri](monitoring) {
  def topic = "DGR-COP-PARAMPLAN-TRI"
  def topicRetry = "DGR-COP-PARAMPLAN-TRI_retry"
  def topicError = "DGR-COP-PARAMPLAN-TRI_error"

  def processInput(input: String): Either[Throwable, ParametricaPlanTri] =
    decode[ParametricaPlanTri](input)

  override def processMessage(registro: ParametricaPlanTri): Future[Response.SuccessProcessing] = {

    val command = ParametricaPlanCommands.ParametricaPlanUpdateFromDto(
      parametricaPlanId = registro.BPP_FPM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor.ask(command)
  }
}
