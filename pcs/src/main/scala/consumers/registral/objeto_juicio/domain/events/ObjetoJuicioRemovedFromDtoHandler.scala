package consumers.registral.objeto_juicio.domain.events

import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.{ObjetoJuicioRemovedFromDto, ObjetoJuicioUpdatedFromDto}
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState

import java.time.LocalDateTime

class ObjetoJuicioRemovedFromDtoHandler {
  def handle(state: ObjetoJuicioState, event: ObjetoJuicioRemovedFromDto): ObjetoJuicioState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now,
        lastDeliveryIdByEvent = event.deliveryId
      )
}
