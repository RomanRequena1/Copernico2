package consumers.registral.exclusiones_objeto.infrastructure.http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoQueries.GetStateExclusionesObjeto
import consumers.registral.exclusiones_objeto.infrastructure.dependency_injection.ExclusionesObjetoActor
import consumers.registral.exclusiones_objeto.infrastructure.json.GetExclusionesObjetoResponseEncoder
import design_principles.actor_model.mechanism.QueryStateAPI
import monitoring.Monitoring

import java.time.LocalDateTime

case class ExclusionesObjetoStateAPI(actor: ExclusionesObjetoActor, monitoring: Monitoring)(implicit
                                                                                           system: ActorSystem[_])
  extends QueryStateAPI(monitoring) {
  import ExclusionesObjetoStateAPI._

  def getState: Route =
    withObjeto { objetoId =>
        queryState(actor, GetStateExclusionesObjeto(objetoId))(
          GetExclusionesObjetoResponseEncoder,
          state => state.fechaUltMod == LocalDateTime.MIN
        )
      }

  override def route: Route = GET(getState)
}


object ExclusionesObjetoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _

}
