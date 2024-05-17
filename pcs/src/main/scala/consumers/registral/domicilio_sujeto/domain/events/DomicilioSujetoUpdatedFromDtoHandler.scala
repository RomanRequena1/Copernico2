package consumers.registral.domicilio_sujeto.domain.events

import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoState

import java.time.LocalDateTime

class DomicilioSujetoUpdatedFromDtoHandler {
  def handle(state: DomicilioSujetoState, event: DomicilioSujetoUpdatedFromDto): DomicilioSujetoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
