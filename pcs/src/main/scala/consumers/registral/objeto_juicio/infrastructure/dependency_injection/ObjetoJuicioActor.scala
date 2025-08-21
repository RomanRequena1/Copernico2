package consumers.registral.objeto_juicio.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.objeto_juicio.application.cqrs.commands.{ObjetoJuicioRemoveFromDtoHandler, ObjetoJuicioUpdateFromDtoHandler}
import consumers.registral.objeto_juicio.application.cqrs.queries.GetStateObjetoJuicioHandler
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.{ObjetoJuicioUpdateFromDto, RemoveObjetoJuicioFromDto}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioMessage
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioQueries.GetStateObjetoJuicio
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.{ObjetoJuicioRemovedFromDto, ObjetoJuicioUpdatedFromDto}
import consumers.registral.objeto_juicio.domain.events.{ObjetoJuicioRemovedFromDtoHandler, ObjetoJuicioUpdatedFromDtoHandler}
import consumers.registral.objeto_juicio.domain.{ObjetoJuicioEvents, ObjetoJuicioState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class ObjetoJuicioActor(state: ObjetoJuicioState = ObjetoJuicioState())(
    implicit
    messageProducer: MessageProducer,
    system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      ObjetoJuicioMessage,
      ObjetoJuicioEvents,
      ObjetoJuicioState
    ](state) {
  this.state.copy(registro = state.registro, lastDeliveryIdByEvent = state.lastDeliveryIdByEvent)

  commandBus.subscribe[ObjetoJuicioUpdateFromDto](new ObjetoJuicioUpdateFromDtoHandler().handle)
  commandBus.subscribe[RemoveObjetoJuicioFromDto](new ObjetoJuicioRemoveFromDtoHandler().handle)
  queryBus.subscribe[GetStateObjetoJuicio](new GetStateObjetoJuicioHandler(this).handle)
  eventBus.subscribe[ObjetoJuicioUpdatedFromDto](new ObjetoJuicioUpdatedFromDtoHandler().handle)
  eventBus.subscribe[ObjetoJuicioRemovedFromDto](new ObjetoJuicioRemovedFromDtoHandler().handle)
}
