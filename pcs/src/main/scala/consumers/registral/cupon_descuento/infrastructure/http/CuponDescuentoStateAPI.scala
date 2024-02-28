package consumers.registral.cupon_descuento.infrastructure.http

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoQueries.GetStateCuponDescuento
import consumers.registral.cupon_descuento.infrastructure.dependency_injection.CuponDescuentoActor
import consumers.registral.cupon_descuento.infrastructure.http.CuponDescuentoStateAPI.{withCuponDescuento, withObjeto, withSujeto, withTipoObjeto}
import consumers.registral.cupon_descuento.infrastructure.json.json.GetCuponDescuentoResponseEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring

import java.time.LocalDateTime

case class CuponDescuentoStateAPI(actor: CuponDescuentoActor, monitoring: Monitoring)(implicit system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI(monitoring) {

  def getState: Route =
    withSujeto { sujetoId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withCuponDescuento { obligacionId =>
            queryState(actor, GetStateCuponDescuento(sujetoId, objetoId, tipoObjeto, obligacionId))(
              GetCuponDescuentoResponseEncoder,
              state => state.fechaUltMod == LocalDateTime.MIN
            )
          }
        }
      }
    }

  def route: Route = GET(getState)
}

object CuponDescuentoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withCuponDescuento = path("cupon_descuento" / Segment)
}

