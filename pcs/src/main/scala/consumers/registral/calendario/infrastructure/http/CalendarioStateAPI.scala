package consumers.registral.calendario.infrastructure.http

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.registral.calendario.application.entities.CalendarioQueries.GetStateCalendario
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.json.json.GetCalendarioResponseEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring

import java.time.LocalDateTime

case class CalendarioStateAPI(actor: CalendarioActor, monitoring: Monitoring)(
    implicit
    system: akka.actor.typed.ActorSystem[_]
) extends QueryStateAPI(monitoring) {
  import CalendarioStateAPI._
  def getState: Route =
    withCalendario { calendarioId =>
      queryState(actor, GetStateCalendario(calendarioId))(
        GetCalendarioResponseEncoder,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }

  def route: Route = GET(getState)
}

object CalendarioStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withCalendario = path("calendario" / Segment)

}
