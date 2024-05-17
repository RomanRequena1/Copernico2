package consumers.registral.declaracion_jurada.domain.events

import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaState

import java.time.LocalDateTime

class DeclaracionJuradaUpdatedFromDtoHandler {
  def handle(state: DeclaracionJuradaState, event: DeclaracionJuradaUpdatedFromDto): DeclaracionJuradaState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
