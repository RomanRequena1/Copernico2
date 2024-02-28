package consumers.registral.subasta.domain.events

import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.domain.SubastaState

import java.time.LocalDateTime

class SubastaUpdatedFromDtoHandler {
  def handle(state: SubastaState, event: SubastaUpdatedFromDto): SubastaState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
