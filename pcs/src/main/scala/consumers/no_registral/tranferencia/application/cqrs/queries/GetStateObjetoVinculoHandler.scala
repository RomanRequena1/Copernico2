package consumers.no_registral.tranferencia.application.cqrs.queries

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoQueries.GetStateObjetoVinculo
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import scala.util.{Success, Try}

class GetStateObjetoVinculoHandler(actor: ObjetoVinculoActor) extends SyncQueryHandler[GetStateObjetoVinculo] {
  override def handle(query: GetStateObjetoVinculo): Try[GetStateObjetoVinculo#ReturnType] = {
    val sender = actor.context.sender()
    val response = GetObjetoVinculoResponse(

      objetoId = actor.state.objetoId,
      fechaUltMod = actor.state.fechaUltMod,
      mapTransf = actor.state.mapTransf,
      mapVinculo = actor.state.mapVinculo,
      tiene30ObjetoVinculo = actor.state.tiene30ObjetoVinculo

    )
//    log.error(s"[${actor.persistenceId}] GetState | $response")
    sender ! response
    Success(response)
  }
}

