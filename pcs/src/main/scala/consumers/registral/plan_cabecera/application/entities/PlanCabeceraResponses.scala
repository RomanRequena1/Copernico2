package consumers.registral.plan_cabecera.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait PlanCabeceraResponses extends CbroSerialization

object PlanCabeceraResponses{
  case class GetPlanCabeceraResponse(registro: Option[PlanCabeceraExternalDto] = None, fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response with CbroSerialization
}
