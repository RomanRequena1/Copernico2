package consumers.registral.plan_cabecera.domain

import consumers.registral.plan_cabecera.application.entities.{PlanCabeceraExternalDto, PlanCabeceraMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class PlanCabeceraState (
  registro: Option[PlanCabeceraExternalDto] = None,
  fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[PlanCabeceraMessage, PlanCabeceraEvents, PlanCabeceraState] with CbroSerialization
