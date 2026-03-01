package consumers.no_registral.objeto.application.cqrs.queries

import akka.pattern.ask
import akka.util.Timeout
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoQueries.{GetAllObnObjeto, GetStateObjeto}
import consumers.no_registral.objeto.application.entities.ObjetoResponses
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetAllObnResponse, GetObjetoResponse, Obligacion}
import consumers.no_registral.objeto.application.helper.InteresCalculator
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.application.entities.ObligacionQueries.GetMiniStateObligacion
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetMiniObligacionResponse
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetAllObnSujetoResponse
import cqrs.untyped.query.QueryHandler.SyncQueryHandler
import serialization.CbroSerialization

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class GetAllObnObjetoHandler(actor: ObjetoActor) extends SyncQueryHandler[GetAllObnObjeto] {
  override def handle(query: GetAllObnObjeto): Try[GetAllObnObjeto#ReturnType] = {
    val sender = actor.context.sender()
    implicit val ec = actor.context.dispatcher

//    println(s"Llego ${query.objetoId} - ${query.tipoObjeto}")

    implicit val timeout: Timeout = 300.seconds
    val obligacionesYObnVencidas: Set[String] = actor.state.obnVencidas.keySet
//    println(s"ChildObjeto ${query.objetoId}: ${actor.state.obligaciones.size}")
    val obligacionesFutures: Set[Future[Obligacion]] = obligacionesYObnVencidas.map { actorObn =>
      actor.self.ask(GetMiniStateObligacion(
        query.sujetoId,
        query.objetoId,
        query.tipoObjeto,
        actorObn
      )).mapTo[GetMiniObligacionResponse].map { response =>
        val interes = InteresCalculator.aplicarInteres(
          capital = response.saldo.getOrElse(BigDecimal(0)),
          vencimiento = response.vencimiento,
          prorroga = response.vencimiento,
          estado = response.estado,
          saldo = response.saldo
        )
        Obligacion(
          id = actorObn,
          saldo = response.saldo,
          interes = Some(interes),
          saldoInteres = response.saldo.map(_ + interes),
          vencimiento = response.vencimiento,
          estado = response.estado,
          tiene30obn = response.tiene30,
          registro = response.registro
        )
      }
    }
//
//    val obligaciones: Set[Obligacion] = Try {
//      val futures = Future.sequence(obligacionesFutures)
//      Await.result(futures, 50.seconds).toSet[Obligacion]
//    }.get

    var lista: Set[Obligacion] = Set.empty

    Future.sequence(obligacionesFutures) onComplete {
      case Success(value) => {
        lista = value
        //println(s"Mi lista: $lista")
        val resultAplicarDescuento: Boolean = {
          if (actor.state.tiene30Sujeto.isDefined) {
            DMNTreintaPorcientoFinal.calcularDmnFinal(
              DmnFinal(
                actor.state.exclusionSujeto,
                actor.state.exclusionObjeto,
                actor.state.clasificacionObjeto,
                actor.state.tiene30Objeto,
                actor.state.tiene30Sujeto.get,
                actor.state.tiene30ObjetoVinculo
              ))
          } else {
            true
          }
        }
        val response = GetAllObnResponse(
          objetoId = query.objetoId,
          objetoTipo = query.tipoObjeto,
          objetoTitularidad = actor.state.registro match {
            case Some(value) => value.SOJ_TITULARIDAD
            case None => None
          },
          tiene30objeto = Some(actor.state.tiene30Objeto),
          aplicarDescuento = Some(resultAplicarDescuento),
//          saldo = actor.state.saldo,
          obligaciones = lista
        )
        sender ! response
      }
      case Failure(ex) => log.error(ex.toString)
    }

    val response = GetAllObnResponse(
      objetoId = query.objetoId,
      objetoTipo = query.tipoObjeto,
      objetoTitularidad = actor.state.registro match {
        case Some(value) => value.SOJ_TITULARIDAD
        case None => None
      },
      tiene30objeto = Some(actor.state.tiene30Objeto),
      aplicarDescuento = actor.state.aplicarDescuento,
//      saldo = actor.state.saldo,
      obligaciones = Set.empty)

//    log.info(s"[${actor.persistenceId}] GetState | $response")
    Success(response)
  }
}
