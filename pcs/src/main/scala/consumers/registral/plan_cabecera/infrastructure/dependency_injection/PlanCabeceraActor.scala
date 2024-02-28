package consumers.registral.plan_cabecera.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.plan_cabecera.application.cqrs.commands.{PlanCabeceraRemoveFromDtoHandler, PlanCabeceraUpdateFromDtoHandler}
import consumers.registral.plan_cabecera.application.cqrs.queries.GetStatePlanCabeceraHandler
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraCommands.PlanCabeceraUpdateFromDto
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraQueries.GetStatePlanCabecera
import consumers.registral.plan_cabecera.application.entities.{PlanCabeceraCommands, PlanCabeceraMessage}
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.{PlanCabeceraRemovedFromDto, PlanCabeceraUpdatedFromDto}
import consumers.registral.plan_cabecera.domain.events.{PlanCabeceraRemovedFromDtoHandler, PlanCabeceraUpdatedFromDtoHandler}
import consumers.registral.plan_cabecera.domain.{PlanCabeceraEvents, PlanCabeceraState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class PlanCabeceraActor(state: PlanCabeceraState = PlanCabeceraState())(
  implicit
  messageProducer: MessageProducer,
  system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
    PlanCabeceraMessage,
    PlanCabeceraEvents,
    PlanCabeceraState
  ](state) {

  commandBus.subscribe[PlanCabeceraUpdateFromDto](new PlanCabeceraUpdateFromDtoHandler().handle)
  commandBus.subscribe[PlanCabeceraCommands.PlanCabeceraRemoveFromDto](new PlanCabeceraRemoveFromDtoHandler().handle)
  queryBus.subscribe[GetStatePlanCabecera](new GetStatePlanCabeceraHandler().handle)
  eventBus.subscribe[PlanCabeceraUpdatedFromDto](new PlanCabeceraUpdatedFromDtoHandler().handle)
  eventBus.subscribe[PlanCabeceraRemovedFromDto](new PlanCabeceraRemovedFromDtoHandler().handle)

}


