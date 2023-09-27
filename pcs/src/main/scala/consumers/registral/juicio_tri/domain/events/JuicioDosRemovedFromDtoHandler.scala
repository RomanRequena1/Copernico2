package consumers.registral.juicio_tri.domain.events


import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosRemovedFromDto
import consumers.registral.juicio_tri.domain.JuicioDosState

import java.time.LocalDateTime

class JuicioDosRemovedFromDtoHandler {
  def handle(state: JuicioDosState, event: JuicioDosRemovedFromDto): JuicioDosState = {
    state.copy(
      lastDeliveryIdByEvents = event.deliveryId,
      registro = Some(event.registro),
      fechaUltMod = LocalDateTime.now()
    )
  }
}
