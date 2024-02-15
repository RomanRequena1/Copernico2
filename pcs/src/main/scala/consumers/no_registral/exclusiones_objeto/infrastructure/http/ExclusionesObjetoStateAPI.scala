package consumers.no_registral.exclusiones_objeto.infrastructure.http

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoQueries.GetStateExclusionesObjeto
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoResponses.GetExclusionesObjetoResponse
import design_principles.actor_model.mechanism.QueryStateAPI
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import monitoring.Monitoring
import consumers.no_registral.exclusiones_objeto.infrastructure.json.ExclusionesObjetoImplicits._

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext

case class ExclusionesObjetoStateAPI (actor: ActorRef, monitoring: Monitoring)(
    implicit
    queryStateApiRequirements: QueryStateApiRequirements
) extends QueryStateAPI(monitoring){
  implicit val system: ActorSystem = queryStateApiRequirements.system
  implicit val ec: ExecutionContext = queryStateApiRequirements.executionContext
  import ExclusionesObjetoStateAPI._


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
          queryState[GetExclusionesObjetoResponse](actorRef = actor, GetStateExclusionesObjeto(objetoId))(
            GetExclusionesObjetoResponseEncoder,
            t => t.fechaUltMod == LocalDateTime.MIN
          )
    }

  //todo va o no va [  ~ POST(developerTools)  ]
  def route: Route = GET(getState)

}

object ExclusionesObjetoStateAPI {

  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _

}


