package consumers.registral.plan_cabecera.application.entities

import consumers.registral.plan_cabecera.application.entities.PlanCabeceraResponses.GetPlanCabeceraResponse
import design_principles.actor_model.Query

sealed trait PlanCabeceraQueries extends Query with PlanCabeceraMessage
object PlanCabeceraQueries{
  case class GetStatePlanCabecera(planCabeceraId: String)
    extends PlanCabeceraQueries{
    override type ReturnType = GetPlanCabeceraResponse
  }
}

