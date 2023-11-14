package consumers.registral.plan_pago_detalles.application.entities

import serialization.CbroSerialization
import java.time.LocalDateTime

sealed trait PlanPagoResponses extends CbroSerialization
object PlanPagoResponses {

  case class GetPlanPagoResponse(registro: Option[PlanPagoExternalDto] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
