package consumers.registral.domicilio_sujeto.infrastructure.http

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.{Directive, Route}
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoQueries.GetStateDomicilioSujeto
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json.getDomicilioSujetoResponseEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring

import java.time.LocalDateTime

case class DomicilioSujetoStateAPI(actor: DomicilioSujetoActor, monitoring: Monitoring)(
    implicit
    system: akka.actor.typed.ActorSystem[_]
) extends QueryStateAPI(monitoring) {
  import DomicilioSujetoStateAPI._
  def getState: Route =
    withSujeto { sujetoId =>
      withDomicilioSujeto { domicilioId =>
        queryState(actor, GetStateDomicilioSujeto(sujetoId, domicilioId))(
          getDomicilioSujetoResponseEncoder,
          state => state.fechaUltMod == LocalDateTime.MIN
        )
      }
    }

  def route: Route = GET(getState)
}

object DomicilioSujetoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withDomicilioSujeto: Directive[Tuple1[String]] = path("domicilio_sujeto" / Segment)
}
