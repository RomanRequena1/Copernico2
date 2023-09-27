package consumers.registral.juicio_tri.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.juicio_tri.application.entities.JuicioDosQueries.GetStateJuicioDos
import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import consumers.registral.juicio_tri.domain.{JuicioDosEvents, JuicioDosState}
import consumers.registral.juicio_tri.infrastructure.dependency_injection.JuicioDosActor
import kafka.MessageProducer

class GetStateJuicioDosHandler(actor: JuicioDosActor)(implicit messageProducer: MessageProducer){

  def handle(
              query: GetStateJuicioDos
            )(state: JuicioDosState)(replyTo: ActorRef[GetJuicioDosResponse]): ReplyEffect[JuicioDosEvents, JuicioDosState] = {
    Effect.reply[
      GetJuicioDosResponse,
      JuicioDosEvents,
      JuicioDosState
    ](replyTo)(
      GetJuicioDosResponse(state.registro, state.lastDeliveryIdByEvents, state.fechaUltMod)
    )
  }
}
