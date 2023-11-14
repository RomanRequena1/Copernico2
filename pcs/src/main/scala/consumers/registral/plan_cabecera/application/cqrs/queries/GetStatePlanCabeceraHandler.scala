package consumers.registral.plan_cabecera.application.cqrs.queries

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraQueries.GetStatePlanCabecera
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraResponses.GetPlanCabeceraResponse
import consumers.registral.plan_cabecera.domain.{PlanCabeceraEvents, PlanCabeceraState}

class GetStatePlanCabeceraHandler() {
  def handle(
            query: GetStatePlanCabecera
  )(state: PlanCabeceraState)(replyTo: ActorRef[GetPlanCabeceraResponse]) =
    Effect.reply[
      GetPlanCabeceraResponse,
      PlanCabeceraEvents,
      PlanCabeceraState
    ](replyTo)(
      GetPlanCabeceraResponse(state.registro, state.fechaUltMod)
    )
}
