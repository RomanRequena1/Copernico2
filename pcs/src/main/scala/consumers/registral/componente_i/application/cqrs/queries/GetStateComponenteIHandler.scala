package consumers.registral.componente_i.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.componente_i.application.entities.ComponenteIQueries.GetStateComponenteI
import consumers.registral.componente_i.application.entities.ComponenteIResponses.GetComponenteIResponse
import consumers.registral.componente_i.domain.{ComponenteIEvents, ComponenteIState}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoQueries.GetStateCuponDescuento
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoResponses.GetCuponDescuentoResponse
import consumers.registral.cupon_descuento.domain.{CuponDescuentoEvents, CuponDescuentoState}

class GetStateComponenteIHandler() {
  def handle(
      query: GetStateComponenteI
  )(state: ComponenteIState)(replyTo: ActorRef[GetComponenteIResponse]) =
    Effect.reply[
      GetComponenteIResponse,
      ComponenteIEvents,
      ComponenteIState
    ](replyTo)(
      GetComponenteIResponse(state.registro, state.detallesComponenteI, state.fechaUltMod)
    )
}
