package consumers.registral.exclusiones_objeto.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoQueries.GetStateExclusionesObjeto
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoResponses.GetExclusionesObjetoResponse
import consumers.registral.exclusiones_objeto.domain.{ExclusionesObjetoEvents, ExclusionesObjetoState}

class GetStateExclusionesObjetoHandler {
  def handle(
              query: GetStateExclusionesObjeto
            )(state: ExclusionesObjetoState)(replyTo: ActorRef[GetExclusionesObjetoResponse]) =
    Effect.reply[
      GetExclusionesObjetoResponse,
      ExclusionesObjetoEvents,
      ExclusionesObjetoState
    ](replyTo)(
      GetExclusionesObjetoResponse(state.registro, state.fechaUltMod)
    )
}
