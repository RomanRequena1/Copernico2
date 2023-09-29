package consumers.registral.plan_pago.domain

import java.time.LocalDateTime
import consumers.registral.plan_pago.application.entities.{PlanPagoExternalDto, PlanPagoMessage, PlanPagoTri}
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class PlanPagoState(
    registro: Option[PlanPagoTri] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[PlanPagoMessage, PlanPagoEvents, PlanPagoState]
