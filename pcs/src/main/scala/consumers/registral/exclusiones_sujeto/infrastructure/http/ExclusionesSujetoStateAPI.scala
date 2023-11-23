package consumers.registral.exclusiones_sujeto.infrastructure.http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.{Directive, Route}
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoQueries.GetStateExclusionesSujeto
import consumers.registral.exclusiones_sujeto.infrastructure.dependency_injection.ExclusionesSujetoActor
import design_principles.actor_model.mechanism.QueryStateAPI
import consumers.registral.exclusiones_sujeto.infrastructure.json.GetExclusionesSujetoResponseEncoder
import monitoring.Monitoring

import java.time.LocalDateTime

case class ExclusionesSujetoStateAPI(actor: ExclusionesSujetoActor, monitoring: Monitoring)(implicit
                                                                                           system: ActorSystem[_])
  extends QueryStateAPI(monitoring) {
  import ExclusionesSujetoStateAPI._

  def getState: Route =
    withSujeto { sujetoId =>
        queryState(actor, GetStateExclusionesSujeto(sujetoId))(
          GetExclusionesSujetoResponseEncoder,
          state => state.fechaUltMod == LocalDateTime.MIN
        )
      }

  override def route: Route = GET(getState)
}



object ExclusionesSujetoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _

}
