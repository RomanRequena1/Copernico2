package consumers.registral.juicio_obn.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.juicio_obn.application.cqrs.commands.{JuicioObnDeleteFromDtoHandler, JuicioObnUpdateFromDtoHandler}
import consumers.registral.juicio_obn.application.cqrs.queries.GetStateJuicioObnHandler
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.{JuicioObnDeleteFromDto, JuicioObnUpdateFromDto}
import consumers.registral.juicio_obn.application.entities.JuicioObnMessage
import consumers.registral.juicio_obn.application.entities.JuicioObnQueries.GetStateJuicioObn
import consumers.registral.juicio_obn.domain.JuicioObnEvents.{JuicioObnDeletedFromDto, JuicioObnUpdatedFromDto}
import consumers.registral.juicio_obn.domain.events.{JuicioObnDeletedFromDtoHandler, JuicioObnUpdatedFromDtoHandler}
import consumers.registral.juicio_obn.domain.{JuicioObnEvents, JuicioObnState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class JuicioObnActor(state: JuicioObnState = JuicioObnState())(
                         implicit
                          messageProducer: MessageProducer,
                         system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
  JuicioObnMessage,
  JuicioObnEvents,
  JuicioObnState
](state){
    this.state.copy(registro = state.registro, lastDeliveryIdByEvent = state.lastDeliveryIdByEvent)

    commandBus.subscribe[JuicioObnDeleteFromDto](new JuicioObnDeleteFromDtoHandler(this).handle)
    commandBus.subscribe[JuicioObnUpdateFromDto](new JuicioObnUpdateFromDtoHandler(this).handle)
    queryBus.subscribe[GetStateJuicioObn](new GetStateJuicioObnHandler(this).handle)
    eventBus.subscribe[JuicioObnUpdatedFromDto](new JuicioObnUpdatedFromDtoHandler().handle)
    eventBus.subscribe[JuicioObnDeletedFromDto](new JuicioObnDeletedFromDtoHandler().handle)

  override def getTags(event: JuicioObnEvents): Set[String] = {
    event match {
      case _ => Set("JuicioObn-updated")
    }
  }
}
