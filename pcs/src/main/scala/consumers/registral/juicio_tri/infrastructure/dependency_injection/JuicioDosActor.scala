package consumers.registral.juicio_tri.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.juicio_tri.application.cqrs.commands.{JuicioDosRemoveFromDtoHandler, JuicioDosUpdateFromDtoHandler}
import consumers.registral.juicio_tri.application.cqrs.queries.GetStateJuicioDosHandler
import consumers.registral.juicio_tri.application.entities.JuicioDosQueries.GetStateJuicioDos
import consumers.registral.juicio_tri.application.entities.{JuicioDosCommands, JuicioDosMessage}
import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosUpdatedFromDto
import consumers.registral.juicio_tri.domain.events.JuicioDosUpdatedFromDtoHandler
import consumers.registral.juicio_tri.domain.{JuicioDosEvents, JuicioDosState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class JuicioDosActor(state: JuicioDosState = JuicioDosState())(
  implicit messageProducer: MessageProducer,
  system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
  JuicioDosMessage,
  JuicioDosEvents,
  JuicioDosState
](state) {

  commandBus.subscribe[JuicioDosCommands.JuicioDosRemoveFromDto](new JuicioDosRemoveFromDtoHandler().handle)
  commandBus.subscribe[JuicioDosCommands.JuicioDosUpdateFromDto](new JuicioDosUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateJuicioDos](new GetStateJuicioDosHandler(this).handle)
  eventBus.subscribe[JuicioDosUpdatedFromDto](new JuicioDosUpdatedFromDtoHandler().handle)
}

