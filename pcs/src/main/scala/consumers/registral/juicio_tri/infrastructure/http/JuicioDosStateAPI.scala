package consumers.registral.juicio_tri.infrastructure.http

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.registral.juicio_tri.application.entities.JuicioDosQueries.GetStateJuicioDos
import consumers.registral.juicio_tri.infrastructure.dependency_injection.JuicioDosActor
import design_principles.actor_model.mechanism.QueryStateAPI
import consumers.registral.juicio_tri.infrastructure.json._
import monitoring.Monitoring
import java.time.LocalDateTime

case class JuicioDosStateAPI (actor: JuicioDosActor, monitoring: Monitoring)(implicit system: akka.actor.typed.ActorSystem[_])
  extends QueryStateAPI(monitoring) {

  import JuicioDosStateAPI._

  def getState: Route =
          withJuicioDos {juicioId =>
            queryState(actor, GetStateJuicioDos(juicioId))(
              GetJuicioDosResponseF,
              state => state.fechaUltMod == LocalDateTime.MIN
            )
          }

  def route: Route = GET(getState)
}

object JuicioDosStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)

  def withJuicioDos = path("juicio" / Segment)

}