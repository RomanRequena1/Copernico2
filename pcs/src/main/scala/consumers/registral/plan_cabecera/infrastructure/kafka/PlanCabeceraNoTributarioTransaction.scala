package consumers.registral.plan_cabecera.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraCommands
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraExternalDto.PlanCabeceraAnt
import consumers.registral.plan_cabecera.infrastructure.dependency_injection.PlanCabeceraActor
import consumers.registral.plan_cabecera.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future


case class PlanCabeceraNoTributarioTransaction(actor: PlanCabeceraActor, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[PlanCabeceraAnt](monitoring) {

  def topic = "DGR-COP-PLAN-CABECERA-ANT"
  def topicRetry = "DGR-COP-PLAN-CABECERA-ANT_retry"
  def topicError = "DGR-COP-PLAN-CABECERA-ANT_error"

  def processInput(input: String): Either[Throwable, PlanCabeceraAnt] =
    decode[PlanCabeceraAnt](input)

  override def processMessage(registro: PlanCabeceraAnt): Future[Response.SuccessProcessing] = {
    val command = PlanCabeceraCommands.PlanCabeceraUpdateFromDto(
      deliveryId = registro.EV_ID,
      planCabeceraId = registro.BPL_IDENTIFICADOR,
      registro = registro
    )
    actor.ask(command)
  }
}
