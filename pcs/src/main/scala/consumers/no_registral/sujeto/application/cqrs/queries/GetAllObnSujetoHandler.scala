package consumers.no_registral.sujeto.application.cqrs.queries

import akka.util.Timeout
import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetAllObnObjeto
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetAllObnResponse, Obligacion}
import consumers.no_registral.obligacion.application.entities.ObligacionQueries.GetMiniStateObligacion
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetMiniObligacionResponse
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.application.entity.SujetoQueries.{GetAllObnSujeto, GetStateSujeto}
import consumers.no_registral.sujeto.application.entity.SujetoResponses.{GetAllObnSujetoResponse, GetSujetoResponse}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

class GetAllObnSujetoHandler(actor: SujetoActor) extends SyncQueryHandler[GetAllObnSujeto] {
  override def handle(query: GetAllObnSujeto): Try[GetAllObnSujeto#ReturnType] = {
    val sender = actor.context.sender()
    implicit val ec = actor.context.dispatcher
    implicit val timeout: Timeout = 50.seconds
    println(s"Child ${actor.state.objetos.size}")
//    println(s"Cantidad hijos para ${query.sujetoId} = ${actor.context.children.size}")
//    println(s"Child ${actor.state.objetos}")

//    actor.state.objetos.map { actorObn =>
//      println(s"CUMBIAb ${actorObn._1} - ${actorObn._2}")
//      actor.context.parent.ask(GetAllObnObjeto(
//        query.sujetoId,
//        actorObn._1,
//        actorObn._2
//      )).mapTo[GetAllObnResponse].map { response =>
//        println(s"CUMBIA- ${response.objetoId}-${response.objetoTipo}")
//        response
//      }
//    }.toSeq

//    actor.state.objetos.foreach { actorObn =>
//      println(s"Antes ${actorObn._1} - ${actorObn._2}")
//      actor.self ! (GetAllObnObjeto(
//        query.sujetoId,
//        actorObn._1,
//        actorObn._2
//      ))
//    }
/*    val getAllObjetosFuture: Seq[Future[GetAllObnResponse]] = actor.state.objetos.map { actorObn =>
      println(s"Antes ${actorObn._1} - ${actorObn._2}")
      actor.self.ask[GetAllObnResponse](GetAllObnObjeto(
        query.sujetoId,
        actorObn._1,
        actorObn._2
      )).map { response =>
        println(s"Dsp- ${response.objetoId}-${response.objetoTipo} - ${response.saldo}")
        response
      }
    }.toSeq*/
//    actor.objetos.map { actorObn =>
//
//      //      val actorSelection = actor.context.actorSelection(actorObn.path)
//      //      val objetoId = actorObn.path.name.split("Objeto-")(1).dropRight(2)
//      //      val tipoObjeto = actorObn.path.name.last.toString
//      println(s"CUMBIAb ${actorObn._1._2} - ${actorObn._1._3}")
//      actorObn._2.ask(GetAllObnObjeto(
//        query.sujetoId,
//        actorObn._1._2,
//        actorObn._1._3
//      )).mapTo[GetAllObnResponse].map { response =>
//        println(s"CUMBIA- ${response.objetoId}-${response.objetoTipo}")
//        response
//      }
//    }

//    val getAllObjetosFuture: Seq[Future[GetAllObnResponse]] = actor.objetos.map { actorObn =>
//
////      val actorSelection = actor.context.actorSelection(actorObn.path)
////      val objetoId = actorObn.path.name.split("Objeto-")(1).dropRight(2)
////      val tipoObjeto = actorObn.path.name.last.toString
//      println(s"CUMBIAb ${actorObn._1._2} - ${actorObn._1._3}")
//      actorObn._2.ask(GetAllObnObjeto(
//        query.sujetoId,
//        actorObn._1._2,
//        actorObn._1._3
//      )).mapTo[GetAllObnResponse].map { response =>
//        println(s"CUMBIA- ${response.objetoId}-${response.objetoTipo}")
//        response
//      }
//    }.toSeq


    val getAllObjetosFuture: Set[Future[GetAllObnResponse]] = actor.state.objetos.map { actorObn =>
//      println(s"Antes ${actorObn._1} - ${actorObn._2}")
      actor.self.ask[GetAllObnResponse](GetAllObnObjeto(
        query.sujetoId,
        actorObn._1,
        actorObn._2
      ))
    }

//    val objetos: Set[GetAllObnResponse] = Try {
//      val futures = Future.sequence(getAllObjetosFuture)
//      Await.result(futures, 50.seconds).toSet[GetAllObnResponse]
//    }.get

    var lista: Set[GetAllObnResponse] = Set.empty
    Future.sequence(getAllObjetosFuture) onComplete {
      case Success(value) => {
        lista = value
//        println(s"Mi lista: $lista")
        val response = GetAllObnSujetoResponse(
          saldo = actor.state.saldo,
          objetos = lista,
          fechaUltMod = actor.state.fechaUltMod,
          registro = actor.state.registro,
          treinta = actor.state.tiene30Sujeto
        )
        sender ! response
      }
      case Failure(ex) => println(ex.toString)
    }

    val response = GetAllObnSujetoResponse(
      saldo = actor.state.saldo,
      objetos = Set.empty,
      fechaUltMod = actor.state.fechaUltMod,
      registro = actor.state.registro,
      treinta = actor.state.tiene30Sujeto
    )
//    log.info(s"[${actor.persistenceId}] GetState | $response")

    Success(response)
  }
}
