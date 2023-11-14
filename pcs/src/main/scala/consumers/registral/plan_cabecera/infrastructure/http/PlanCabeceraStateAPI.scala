package consumers.registral.plan_cabecera.infrastructure.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraQueries.GetStatePlanCabecera
import consumers.registral.plan_cabecera.infrastructure.dependency_injection.PlanCabeceraActor
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring
import consumers.registral.plan_cabecera.infrastructure.json.json._
import java.time.LocalDateTime

case class PlanCabeceraStateAPI(actor: PlanCabeceraActor, monitoring: Monitoring)(implicit
                                                                                  system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI(monitoring){
  import PlanCabeceraStateAPI._

  def getState: Route =
    withPlanCabecera{ planCabeceraId =>
      queryState(actor, GetStatePlanCabecera(planCabeceraId))(
        PlanCabeceraResponsesEncoder,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }
  def route: Route = GET(getState)
}

object PlanCabeceraStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withPlanCabecera = path("plan_cabecera" / Segment)
}