package consumers.registral.cupon_descuento.domain.events

import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.CuponDescuentoUpdatedFromDto
import consumers.registral.cupon_descuento.domain.CuponDescuentoState

import java.time.LocalDateTime

class CuponDescuentoUpdatedFromDtoHandler {
  def handle(state: CuponDescuentoState, event: CuponDescuentoUpdatedFromDto): CuponDescuentoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
