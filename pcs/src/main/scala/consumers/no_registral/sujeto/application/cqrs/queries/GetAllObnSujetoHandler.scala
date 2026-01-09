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

//    val getAllObjetosFuture: Set[Future[GetAllObnResponse]] = actor.state.objetos.map { actorObn =>
////      println(s"Antes ${actorObn._1} - ${actorObn._2}")
//      actor.self.ask[GetAllObnResponse](GetAllObnObjeto(
//        query.sujetoId,
//        actorObn._1,
//        actorObn._2
//      ))
//    }
  def calcularSaldoInteresTotal(objetos: Set[GetAllObnResponse]): BigDecimal = {
    objetos.flatMap(_.obligaciones).flatMap(_.saldoInteres).sum
  }

  val getAllObjetosFutureList: Future[Set[GetAllObnResponse]] =  Future.traverse(actor.state.objetos)(obj => {
      actor.self.ask[GetAllObnResponse](GetAllObnObjeto(
        query.sujetoId,
        obj._1,
        obj._2
      ))
    })
//    val objetos: Set[GetAllObnResponse] = Try {
//      val futures = Future.sequence(getAllObjetosFuture)
//      Await.result(futures, 50.seconds).toSet[GetAllObnResponse]
//    }.get

    var list: Set[GetAllObnResponse] = Set.empty
    getAllObjetosFutureList onComplete {
      case Success(value) => {
        list = value
        //        println(s"Mi lista: $lista")
        val response = GetAllObnSujetoResponse(
          saldo = actor.state.saldo,
          saldoTotal = calcularSaldoInteresTotal(list),
          tiene30sujeto = Some(actor.state.tiene30Sujeto),
          objetos = list,
          fechaUltMod = actor.state.fechaUltMod,
          registro = actor.state.registro,
          treinta = actor.state.tiene30Sujeto
        )
        sender ! response
      }
      case Failure(ex) => log.error(ex.toString)
    }
//    Future.sequence(getAllObjetosFuture) onComplete {
//      case Success(value) => {
//        lista = value
////        println(s"Mi lista: $lista")
//        val response = GetAllObnSujetoResponse(
//          saldo = actor.state.saldo,
//          objetos = lista,
//          fechaUltMod = actor.state.fechaUltMod,
//          registro = actor.state.registro,
//          treinta = actor.state.tiene30Sujeto
//        )
//        sender ! response
//      }
//      case Failure(ex) => println(ex.toString)
//    }

    val response = GetAllObnSujetoResponse(
      saldo = actor.state.saldo,
      saldoTotal = 0,
      tiene30sujeto = Some(actor.state.tiene30Sujeto),
      objetos = Set.empty,
      fechaUltMod = actor.state.fechaUltMod,
      registro = actor.state.registro,
      treinta = actor.state.tiene30Sujeto
    )
//    log.info(s"[${actor.persistenceId}] GetState | $response")

    Success(response)
  }
}
