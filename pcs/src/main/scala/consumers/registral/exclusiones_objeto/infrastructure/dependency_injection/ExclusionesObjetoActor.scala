package consumers.registral.exclusiones_objeto.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.exclusiones_objeto.application.cqrs.commands.ExclusionesObjetoUpdateFromDtoHandler
import consumers.registral.exclusiones_objeto.application.cqrs.queries.GetStateExclusionesObjetoHandler
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoMessage
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoQueries.GetStateExclusionesObjeto
import consumers.registral.exclusiones_objeto.domain.ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
import consumers.registral.exclusiones_objeto.domain.events.ExclusionesObjetoUpdatedFromDtoHandler
import consumers.registral.exclusiones_objeto.domain.{ExclusionesObjetoEvents, ExclusionesObjetoState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class ExclusionesObjetoActor(state: ExclusionesObjetoState = ExclusionesObjetoState())(
  implicit

  messageProducer: MessageProducer,
  system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
  ExclusionesObjetoMessage,
  ExclusionesObjetoEvents,
  ExclusionesObjetoState
](state) {

  commandBus.subscribe[ExclusionesObjetoUpdateFromDto](new ExclusionesObjetoUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateExclusionesObjeto](new GetStateExclusionesObjetoHandler().handle)
  eventBus.subscribe[ExclusionesObjetoUpdatedFromDto](new ExclusionesObjetoUpdatedFromDtoHandler().handle)
}