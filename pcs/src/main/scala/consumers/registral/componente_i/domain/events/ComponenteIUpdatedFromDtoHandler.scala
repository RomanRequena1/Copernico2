package consumers.registral.componente_i.domain.events

import consumers.registral.componente_i.domain.ComponenteIEvents.ComponenteIUpdatedFromDto
import consumers.registral.componente_i.domain.ComponenteIState

import java.time.LocalDateTime

class ComponenteIUpdatedFromDtoHandler {
  def handle(state: ComponenteIState, event: ComponenteIUpdatedFromDto): ComponenteIState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
