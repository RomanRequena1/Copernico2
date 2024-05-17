package consumers.registral.juicio_obn.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.juicio_obn.application.entities.JuicioObnQueries.GetStateJuicioObn
import consumers.registral.juicio_obn.application.entities.JuicioObnResponses.GetJuicioObnResponses
import consumers.registral.juicio_obn.domain.{JuicioObnEvents, JuicioObnState}
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import kafka.MessageProducer

class GetStateJuicioObnHandler(actor: JuicioObnActor)(implicit messageProducer: MessageProducer){

  def handle(
              query: GetStateJuicioObn
            )(state: JuicioObnState)(replyTo: ActorRef[GetJuicioObnResponses]): ReplyEffect[JuicioObnEvents, JuicioObnState] = {
    Effect.reply[
      GetJuicioObnResponses,
      JuicioObnEvents,
      JuicioObnState
    ](replyTo)(
      GetJuicioObnResponses(state.registro, state.lastDeliveryIdByEvent, state.fechaUltMod)
    )
  }
}



