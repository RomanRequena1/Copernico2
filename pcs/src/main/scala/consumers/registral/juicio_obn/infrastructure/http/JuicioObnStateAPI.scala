package consumers.registral.juicio_obn.infrastructure.http
/*
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.registral.juicio_obn.application.entities.JuicioObnQueries.GetStateJuicioObn
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring
import consumers.registral.juicio_obn.infrastructure.json._

import java.time.LocalDateTime

case class JuicioObnStateAPI(actor: JuicioObnActor, monitoring: Monitoring)(implicit system: akka.actor.typed.ActorSystem[_])
  extends QueryStateAPI(monitoring){
  import JuicioObnStateAPI._

  private def getState: Route =
    withJuicio { juicioObnId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withObligacion { obligacionId =>
            queryState(actor, GetStateJuicioObn(juicioObnId, objetoId, tipoObjeto, obligacionId))(
              GetJuicioObnResponseF,
              state => state.fechasUltMod == LocalDateTime.MIN
            )
          }
        }
      }
    }
  def route: Route = GET(getState)
}
object JuicioObnStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)

  def withJuicio: (String => Route) => Route = nestedRoute("juicioObn")

  def withObjeto: (String => Route) => Route = nestedRoute("objeto")

  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo")

  def withObligacion = path("obligacion" / Segment)
}*/