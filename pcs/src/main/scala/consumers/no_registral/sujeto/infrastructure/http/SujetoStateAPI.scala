package consumers.no_registral.sujeto.infrastructure.http

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import consumers.no_registral.obligacion.infrastructure.http.ObligacionStateAPI.withSujeto
import consumers.no_registral.sujeto.application.entity.SujetoQueries.{GetAllObnSujeto, GetSnapshotSujeto, GetStateSujeto}
import consumers.no_registral.sujeto.application.entity.SujetoResponses.{GetAllObnSujetoResponse, GetSujetoResponse}
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits._
import design_principles.actor_model.mechanism.QueryStateAPI
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import monitoring.Monitoring

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
case class SujetoStateAPI(actor: ActorRef, monitoring: Monitoring)(
    implicit
    queryStateApiRequirements: QueryStateApiRequirements
) extends QueryStateAPI(monitoring) {

  implicit val system: ActorSystem = queryStateApiRequirements.system
  implicit val ec: ExecutionContext = queryStateApiRequirements.executionContext

  private var allObnActors: Option[ActorRef] = None

  def developerTools: Route =
    withSujeto { sujetoId =>
      withDeveloperTools { command =>
        command match {
          case "sleep" =>
            log.info(s"[Sujeto-$sujetoId] ${Console.RED} Going to sleep. ${Console.RESET}")
            actor ! PoisonPill
        }
        complete { HttpResponse(OK) }
      }
    }

  def getAllActorObn: Route =
    path("sujeto" / Segment / "getallactorobn") {
      case "1" => {
        if (allObnActors.isEmpty) {
          val actorRef = system.actorOf(
            ObligacionTaggingActor.props(actor),
            "obligacion-tagging-actor"
          )
          allObnActors = Some(actorRef)
          actorRef ! StartReprocessing()
          complete(HttpResponse(OK, entity = "Tagging process started"))
        } else {
          complete(HttpResponse(OK, entity = "Tagging process already running"))
        }
      }
      case "0" => {
        allObnActors match {
          case Some(actorRef) =>
            actorRef ! StopReprocessing()
            allObnActors = None
            complete(HttpResponse(OK, entity = "Tagging process stopped"))
          case None =>
            complete(HttpResponse(OK, entity = "No tagging process running"))
        }
      }
      case _ => complete {
        HttpResponse(OK)
      }
    }

  def getState: Route =
    path("sujeto" / Segment) { sujetoId =>
      queryState[GetSujetoResponse](actor, GetStateSujeto(sujetoId))(
        GetSujetoResponseEncoder,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }

  def getStateAll: Route =
    path("sujeto" / Segment / "all") { sujetoId =>
      queryState[GetAllObnSujetoResponse](actor, GetAllObnSujeto(sujetoId))(
        GetAllObnSujetoResponseEncoder,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }

  def getSnapshot: Route =
    path("sujeto" / Segment / "snapshot") { sujetoId =>
      queryState[GetSujetoResponse](actor, GetSnapshotSujeto(sujetoId))(
        GetSujetoResponseEncoder,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }

  def route: Route = GET(getState) ~ GET(developerTools) ~ GET(getSnapshot) ~ GET(getAllActorObn) ~ GET(getStateAll)
  def withDeveloperTools = path("developer" / "tools" / Segment)
}
