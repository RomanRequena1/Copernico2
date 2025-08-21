package consumers.registral.objeto_juicio.domain.events

import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState

import java.time.LocalDateTime

class ObjetoJuicioUpdatedFromDtoHandler {
  def handle(state: ObjetoJuicioState, event: ObjetoJuicioUpdatedFromDto): ObjetoJuicioState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now,
        lastDeliveryIdByEvent = event.deliveryId
      )
}
