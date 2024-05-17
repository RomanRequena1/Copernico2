package consumers.registral.tramite.domain.events

import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.domain.TramiteState

import java.time.LocalDateTime

class TramiteUpdatedFromDtoHandler {
  def handle(state: TramiteState, event: TramiteUpdatedFromDto): TramiteState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
