package consumers.registral.juicio.domain.events

import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import consumers.registral.juicio.domain.JuicioState

import java.time.LocalDateTime

class JuicioUpdatedFromDtoHandler {
  def handle(state: JuicioState, event: JuicioUpdatedFromDto): JuicioState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
