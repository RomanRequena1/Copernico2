package consumers.registral.juicio_obn.domain.events

import consumers.registral.juicio_obn.domain.JuicioObnEvents.JuicioObnDeletedFromDto
import consumers.registral.juicio_obn.domain.JuicioObnState

import java.time.LocalDateTime

class JuicioObnDeletedFromDtoHandler {
  def handle(state: JuicioObnState, event: JuicioObnDeletedFromDto): JuicioObnState = {
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now,
        lastDeliveryIdByEvent = event.deliveryId
      )
  }
}
