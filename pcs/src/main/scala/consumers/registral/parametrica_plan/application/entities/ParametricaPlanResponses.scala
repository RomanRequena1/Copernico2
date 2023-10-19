package consumers.registral.parametrica_plan.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ParametricaPlanResponses extends CbroSerialization
object ParametricaPlanResponses {

  case class GetParametricaPlanResponse(registro: Option[ParametricaPlanExternalDto] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
