package consumers.registral.plan_pago.domain

import consumers.registral.plan_pago.application.entities.{PlanPagoExternalDto, PlanPagoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS

import java.time.LocalDateTime

case class PlanPagoState(
    registro: Option[PlanPagoExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[PlanPagoMessage, PlanPagoEvents, PlanPagoState]
