package consumers.registral.plan_cabecera.domain.events

import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.PlanCabeceraRemovedFromDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraState

import java.time.LocalDateTime

class PlanCabeceraRemovedFromDtoHandler {
  def handle(state: PlanCabeceraState, event: PlanCabeceraRemovedFromDto): PlanCabeceraState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
