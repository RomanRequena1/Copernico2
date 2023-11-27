package consumers.registral.exclusiones_sujeto.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.exclusiones_sujeto.application.cqrs.commands.ExclusionesSujetoUpdateFromDtoHandler
import consumers.registral.exclusiones_sujeto.application.cqrs.queries.GetStateExclusionesSujetoHandler
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoCommands.ExclusionesSujetoUpdateFromDto
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoMessage
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoQueries.GetStateExclusionesSujeto
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
import consumers.registral.exclusiones_sujeto.domain.events.ExclusionesSujetoUpdatedFromDtoHandler
import consumers.registral.exclusiones_sujeto.domain.{ExclusionesSujetoEvents, ExclusionesSujetoState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class ExclusionesSujetoActor(state: ExclusionesSujetoState = ExclusionesSujetoState())(
  implicit

  messageProducer: MessageProducer,
  system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
  ExclusionesSujetoMessage,
  ExclusionesSujetoEvents,
  ExclusionesSujetoState
](state) {

  commandBus.subscribe[ExclusionesSujetoUpdateFromDto](new ExclusionesSujetoUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateExclusionesSujeto](new GetStateExclusionesSujetoHandler().handle)
  eventBus.subscribe[ExclusionesSujetoUpdatedFromDto](new ExclusionesSujetoUpdatedFromDtoHandler().handle)
}