package consumers.registral.plan_pago_detalles.domain.events

import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import consumers.registral.plan_pago_detalles.domain.PlanPagoState

import java.time.LocalDateTime

class PlanPagoUpdatedFromDtoHandler {
  def handle(state: PlanPagoState, event: PlanPagoUpdatedFromDto): PlanPagoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
