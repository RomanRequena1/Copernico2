package consumers.registral.parametrica_recargo.domain.events

import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoState

import java.time.LocalDateTime

class ParametricaRecargoUpdatedFromDtoHandler {
  def handle(state: ParametricaRecargoState, event: ParametricaRecargoUpdatedFromDto): ParametricaRecargoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
