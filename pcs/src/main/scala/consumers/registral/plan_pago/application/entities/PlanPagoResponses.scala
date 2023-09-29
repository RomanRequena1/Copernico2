package consumers.registral.plan_pago.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait PlanPagoResponses extends CbroSerialization
object PlanPagoResponses {

  case class GetPlanPagoResponse(registro: Option[PlanPagoTri] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
