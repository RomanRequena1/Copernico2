package consumers.registral.plan_pago_detalles.domain

import consumers.registral.plan_pago_detalles.application.entities.{PlanPagoExternalDto, PlanPagoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class PlanPagoState(
    registro: Option[PlanPagoExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[PlanPagoMessage, PlanPagoEvents, PlanPagoState] with CbroSerialization
