package consumers.registral.objeto_juicio.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.juicio.application.entities.JuicioQueries.GetStateJuicio
import consumers.registral.juicio.application.entities.JuicioResponses.GetJuicioResponse
import consumers.registral.juicio.domain.{JuicioEvents, JuicioState}
import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioQueries.GetStateObjetoJuicio
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioResponses.GetObjetoJuicioResponse
import consumers.registral.objeto_juicio.domain.{ObjetoJuicioEvents, ObjetoJuicioState}
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import kafka.MessageProducer

class GetStateObjetoJuicioHandler(actor: ObjetoJuicioActor)(implicit messageProducer: MessageProducer) {
  def handle(
      query: GetStateObjetoJuicio
  )(state: ObjetoJuicioState)(replyTo: ActorRef[GetObjetoJuicioResponse]) =
    Effect.reply[
      GetObjetoJuicioResponse,
      ObjetoJuicioEvents,
      ObjetoJuicioState
    ](replyTo)(
      GetObjetoJuicioResponse(state.registro, state.fechaUltMod)
    )
}
