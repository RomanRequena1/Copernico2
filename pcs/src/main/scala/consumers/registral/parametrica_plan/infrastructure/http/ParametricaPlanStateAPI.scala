package consumers.registral.parametrica_plan.infrastructure.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Route}
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanQueries.GetStateParametricaPlan
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.json.ParametricaPlanImplicits.GetParametricaPlanResponseEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring

import java.time.LocalDateTime

case class ParametricaPlanStateAPI(actor: ParametricaPlanActor, monitoring: Monitoring)(
    implicit
    system: akka.actor.typed.ActorSystem[_]
) extends QueryStateAPI(monitoring) {
  import ParametricaPlanStateAPI._

  def getState: Route =
    withParametricaPlan { parametricaPlanId =>
      queryState(actor, GetStateParametricaPlan(parametricaPlanId))(
        GetParametricaPlanResponseEncoder,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }

  def route: Route = GET(getState)
}

object ParametricaPlanStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withParametricaPlan: Directive[Tuple1[String]] = path("parametrica_plan" / Segment)
}
