package consumers.registral.juicio_obn.domain.events

import consumers.registral.juicio_obn.domain.JuicioObnEvents.JuicioObnUpdatedFromDto
import consumers.registral.juicio_obn.domain.JuicioObnState

import java.time.LocalDateTime

class JuicioObnUpdatedFromDtoHandler {
  def handle(state: JuicioObnState, event: JuicioObnUpdatedFromDto): JuicioObnState = {
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now,
        lastDeliveryIdByEvent = event.deliveryId
      )
  }
}
