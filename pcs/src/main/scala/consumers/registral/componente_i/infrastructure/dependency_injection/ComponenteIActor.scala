package consumers.registral.componente_i.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.componente_i.application.cqrs.commands.ComponenteIUpdateFromDtoHandler
import consumers.registral.componente_i.application.cqrs.queries.GetStateComponenteIHandler
import consumers.registral.componente_i.application.entities.ComponenteICommands.ComponenteIUpdateFromDto
import consumers.registral.componente_i.application.entities.ComponenteIMessage
import consumers.registral.componente_i.application.entities.ComponenteIQueries.GetStateComponenteI
import consumers.registral.componente_i.domain.ComponenteIEvents.ComponenteIUpdatedFromDto
import consumers.registral.componente_i.domain.events.ComponenteIUpdatedFromDtoHandler
import consumers.registral.componente_i.domain.{ComponenteIEvents, ComponenteIState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class ComponenteIActor(state: ComponenteIState =  ComponenteIState())(
  implicit
  messageProducer: MessageProducer,
  system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
  ComponenteIMessage,
  ComponenteIEvents,
  ComponenteIState
](state) {

  commandBus.subscribe[ComponenteIUpdateFromDto](new ComponenteIUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateComponenteI](new GetStateComponenteIHandler().handle)
  eventBus.subscribe[ComponenteIUpdatedFromDto](new ComponenteIUpdatedFromDtoHandler().handle)
}
