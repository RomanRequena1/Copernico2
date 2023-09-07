package consumers.registral.componente_i.infrastructure.http

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.registral.componente_i.application.entities.ComponenteIQueries.GetStateComponenteI
import consumers.registral.componente_i.infrastructure.dependency_injection.ComponenteIActor
import consumers.registral.componente_i.infrastructure.http.ComponenteIStateAPI.withComponenteI
import consumers.registral.cupon_descuento.infrastructure.http.CuponDescuentoStateAPI._
import design_principles.actor_model.mechanism.QueryStateAPI
import consumers.registral.componente_i.infrastructure.json._
import monitoring.Monitoring

import java.time.LocalDateTime

case class ComponenteIStateAPI(actor: ComponenteIActor, monitoring: Monitoring)(implicit system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI(monitoring) {

  def getState: Route =
    withSujeto { sujetoId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withComponenteI { obligacionId =>
            queryState(actor, GetStateComponenteI(sujetoId, objetoId, tipoObjeto, obligacionId))(
              GetComponenteIResponseF,
              state => state.fechaUltMod == LocalDateTime.MIN
            )
          }
        }
      }
    }

  def route: Route = GET(getState)
}

object ComponenteIStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withComponenteI = path("obligacion" / Segment)
}

