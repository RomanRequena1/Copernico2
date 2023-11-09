package consumers.registral.plan_pago_detalles.application.entities

import consumers.registral.plan_pago_detalles.application.entities.PlanPagoResponses.GetPlanPagoResponse
import design_principles.actor_model.Query

sealed trait PlanPagoQueries extends Query with PlanPagoMessage

object PlanPagoQueries {
  case class GetStatePlanPago(planPagoId: String,
                              tipoObjeto: String,
                              objetoId: String,
                              obligacionId: String)
    extends PlanPagoQueries {
    override type ReturnType = GetPlanPagoResponse
  }
}
