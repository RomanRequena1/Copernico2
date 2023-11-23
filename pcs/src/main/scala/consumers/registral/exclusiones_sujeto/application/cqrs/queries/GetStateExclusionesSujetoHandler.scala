package consumers.registral.exclusiones_sujeto.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoQueries.GetStateExclusionesSujeto
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoResponses.GetExclusionesSujetoResponse
import consumers.registral.exclusiones_sujeto.domain.{ExclusionesSujetoEvents, ExclusionesSujetoState}

class GetStateExclusionesSujetoHandler {
  def handle(
              query: GetStateExclusionesSujeto
            )(state: ExclusionesSujetoState)(replyTo: ActorRef[GetExclusionesSujetoResponse]) =
    Effect.reply[
      GetExclusionesSujetoResponse,
      ExclusionesSujetoEvents,
      ExclusionesSujetoState
    ](replyTo)(
      GetExclusionesSujetoResponse(state.registro, state.fechaUltMod)
    )
}
