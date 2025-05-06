package consumers.registral.objeto_juicio.infrastructure.http

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioQueries.GetStateObjetoJuicio
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import consumers.registral.objeto_juicio.infrastructure.json.GetObjetoJuicioResponseEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring
import java.time.LocalDateTime


case class ObjetoJuicioStateAPI(actor: ObjetoJuicioActor, monitoring: Monitoring)(implicit system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI(monitoring) {
  import ObjetoJuicioStateAPI._

  def getState: Route =
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withJuicio { juicioId =>
            withPlan { planId =>
              queryState(actor, GetStateObjetoJuicio(objetoId, tipoObjeto, juicioId, planId))(
                GetObjetoJuicioResponseEncoder,
                state => state.fechaUltMod == LocalDateTime.MIN
              )
            }
          }
        }
      }


  def route: Route = GET(getState)
}

object ObjetoJuicioStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withJuicio = path("juicio" / Segment)
  def withPlan = path("plan" / Segment)

}
