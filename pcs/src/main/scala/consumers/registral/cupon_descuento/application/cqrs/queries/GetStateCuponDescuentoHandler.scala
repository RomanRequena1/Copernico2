package consumers.registral.cupon_descuento.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoQueries.GetStateCuponDescuento
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoResponses.GetCuponDescuentoResponse
import consumers.registral.cupon_descuento.domain.{CuponDescuentoEvents, CuponDescuentoState}

class GetStateCuponDescuentoHandler() {
  def handle(
      query: GetStateCuponDescuento
  )(state: CuponDescuentoState)(replyTo: ActorRef[GetCuponDescuentoResponse]) =
    Effect.reply[
      GetCuponDescuentoResponse,
      CuponDescuentoEvents,
      CuponDescuentoState
    ](replyTo)(
      GetCuponDescuentoResponse(state.registro, state.detallesCuponDescuento, state.fechaUltMod)
    )
}
