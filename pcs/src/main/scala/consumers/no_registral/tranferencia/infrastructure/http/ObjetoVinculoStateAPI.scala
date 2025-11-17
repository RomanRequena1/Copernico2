package consumers.no_registral.tranferencia.infrastructure.http



import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoQueries.GetStateObjetoVinculo
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import consumers.no_registral.tranferencia.infrastructure.http.ObjetoVinculoStateAPI.withObjeto
import consumers.no_registral.tranferencia.infrastructure.json.ObjetoVinculoImplicits._
import design_principles.actor_model.mechanism.QueryStateAPI
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import monitoring.Monitoring

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

case class ObjetoVinculoStateAPI(actor: ActorRef, monitoring: Monitoring)(
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
        queryState[GetObjetoVinculoResponse](actorRef = actor, GetStateObjetoVinculo(objetoId))(
          GetObjetoVinculoResponseEncoder,
          t => t.fechaUltMod == LocalDateTime.MIN
        )
      }

  def route: Route = GET(getState) ~ POST(developerTools)

}

object ObjetoVinculoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withObjeto: (String => Route) => Route = nestedRoute("objetovinculo") _

}
