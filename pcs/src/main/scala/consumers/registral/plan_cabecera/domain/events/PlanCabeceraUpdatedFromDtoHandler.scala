package consumers.registral.plan_cabecera.domain.events

import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.PlanCabeceraUpdatedFromDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraState

import java.time.LocalDateTime

class PlanCabeceraUpdatedFromDtoHandler {
  def handle(state: PlanCabeceraState, event: PlanCabeceraUpdatedFromDto): PlanCabeceraState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
