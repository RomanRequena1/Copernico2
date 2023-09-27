package consumers.registral.juicio_tri.domain.events

import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosUpdatedFromDto
import consumers.registral.juicio_tri.domain.JuicioDosState

import java.time.LocalDateTime

class JuicioDosUpdatedFromDtoHandler {
def handle(state: JuicioDosState, event: JuicioDosUpdatedFromDto): JuicioDosState =
  state
    .copy(
      registro = Some(event.registro),
      fechaUltMod = LocalDateTime.now
    )
}
