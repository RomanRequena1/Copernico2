package consumers.registral.exclusiones_sujeto.domain.events

import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoState

import java.time.LocalDateTime

class ExclusionesSujetoUpdatedFromDtoHandler {
  def handle(state: ExclusionesSujetoState, event: ExclusionesSujetoUpdatedFromDto): ExclusionesSujetoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
