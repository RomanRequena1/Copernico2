package consumers.registral.plan_pago_detalles.domain.events

import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.PlanPagoRemovedFromDto
import consumers.registral.plan_pago_detalles.domain.PlanPagoState

import java.time.LocalDateTime

class PlanPagoRemovedFromDtoHandler {
  def handle(state: PlanPagoState, event: PlanPagoRemovedFromDto): PlanPagoState =
    state
      .copy(
        registro = Some(event.registro),
        fechaUltMod = LocalDateTime.now
      )
}
