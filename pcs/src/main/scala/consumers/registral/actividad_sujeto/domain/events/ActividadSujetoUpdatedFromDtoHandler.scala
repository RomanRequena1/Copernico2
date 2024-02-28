package consumers.registral.actividad_sujeto.domain.events

import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import consumers.registral.actividad_sujeto.domain.ActividadSujetoState

import java.time.LocalDateTime

class ActividadSujetoUpdatedFromDtoHandler {
  def handle(state: ActividadSujetoState, event: ActividadSujetoUpdatedFromDto): ActividadSujetoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
