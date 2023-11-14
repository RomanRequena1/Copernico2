package consumers.registral.plan_pago_detalles.infrastructure.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoQueries.GetStatePlanPago
import consumers.registral.plan_pago_detalles.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago_detalles.infrastructure.json.json.PlanPagoResponsesEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring

import java.time.LocalDateTime

case class PlanPagoStateAPI(actor: PlanPagoActor, monitoring: Monitoring)(implicit
                                                                          system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI(monitoring) {
  import PlanPagoStateAPI._

  def getState: Route =
    withPlanPago { planPagoId =>
      withTipoObjeto { tipoObjeto =>
        withObjeto { objetoId =>
          withObligacion { obligacionId =>
            queryState(actor, GetStatePlanPago(planPagoId, tipoObjeto, objetoId, obligacionId))(
              PlanPagoResponsesEncoder,
              state => state.fechaUltMod == LocalDateTime.MIN
            )
          }
        }
      }
    }

  def route: Route = GET(getState)
}

object PlanPagoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withPlanPago = path("plan_pago" / Segment)
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withObligacion = path("obligacion" / Segment)

}
