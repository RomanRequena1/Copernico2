package consumers.registral.plan_pago_detalles.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.plan_pago_detalles.application.cqrs.commands.PlanPagoUpdateFromDtoHandler
import consumers.registral.plan_pago_detalles.application.cqrs.queries.GetStatePlanPagoHandler
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoCommands.PlanPagoUpdateFromDto
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoMessage
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoQueries.GetStatePlanPago
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import consumers.registral.plan_pago_detalles.domain.events.PlanPagoUpdatedFromDtoHandler
import consumers.registral.plan_pago_detalles.domain.{PlanPagoEvents, PlanPagoState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import design_principles.actor_model.Response
import kafka.MessageProducer

import scala.concurrent.Future

case class PlanPagoActor(state: PlanPagoState = PlanPagoState())(
    implicit
    messageProducer: MessageProducer,
    system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      PlanPagoMessage,
      PlanPagoEvents,
      PlanPagoState
    ](state) {

  commandBus.subscribe[PlanPagoUpdateFromDto](new PlanPagoUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStatePlanPago](new GetStatePlanPagoHandler().handle)
  eventBus.subscribe[PlanPagoUpdatedFromDto](new PlanPagoUpdatedFromDtoHandler().handle)
}
