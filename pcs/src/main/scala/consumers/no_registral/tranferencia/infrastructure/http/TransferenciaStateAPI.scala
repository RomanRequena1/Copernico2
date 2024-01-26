package consumers.no_registral.tranferencia.infrastructure.http



import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.no_registral.objeto.application.entities.ObjetoQueries.{GetSnapshotObjeto, GetStateExencion, GetStateObjeto}
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetExencionResponse, GetObjetoResponse}
import consumers.no_registral.tranferencia.infrastructure.json.TranferenciasImplicits._
import consumers.no_registral.tranferencia.application.entity.TransferenciaQueries.GetStateTransferencia
import consumers.no_registral.tranferencia.application.entity.TransferenciaResponses.GetTransferenciaResponse
import consumers.no_registral.tranferencia.infrastructure.http.TransferenciaStateAPI.withObjeto
import design_principles.actor_model.mechanism.QueryStateAPI
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import monitoring.Monitoring

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

case class TransferenciaStateAPI(actor: ActorRef, monitoring: Monitoring)(
  implicit
  queryStateApiRequirements: QueryStateApiRequirements
) extends QueryStateAPI(monitoring) {

  implicit val system: ActorSystem = queryStateApiRequirements.system
  implicit val ec: ExecutionContext = queryStateApiRequirements.executionContext


  def developerTools: Route =
      withObjeto { objetoId =>
          path("developer" / "tools" / Segment) { command =>
            command match {
              case "sleep" =>
                log.info(
                  s"[Objeto-$objetoId] ${Console.RED} Going to sleep. ${Console.RESET}"
                )
                actor ! PoisonPill
            }

            complete { HttpResponse(OK) }
          }
        }

  def getState: Route =
      withObjeto { objetoId =>
          queryState[GetTransferenciaResponse](actorRef = actor, GetStateTransferencia(objetoId))(
            GetTransferenciaResponseEncoder,
            t => t.fechaUltMod == LocalDateTime.MIN
          )
        }

  def route: Route = GET(getState) ~ POST(developerTools)

}

object TransferenciaStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _

}
