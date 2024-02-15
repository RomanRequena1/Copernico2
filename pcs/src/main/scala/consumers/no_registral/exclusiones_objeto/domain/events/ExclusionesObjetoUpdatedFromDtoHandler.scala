package consumers.no_registral.exclusiones_objeto.domain.events

import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoState

import java.time.LocalDateTime

class ExclusionesObjetoUpdatedFromDtoHandler {
  def handle(state: ExclusionesObjetoState, event: ExclusionesObjetoUpdatedFromDto): ExclusionesObjetoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}

