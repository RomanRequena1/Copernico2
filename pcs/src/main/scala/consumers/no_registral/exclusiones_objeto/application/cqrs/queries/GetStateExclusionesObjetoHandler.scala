package consumers.no_registral.exclusiones_objeto.application.cqrs.queries

import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoQueries.GetStateExclusionesObjeto
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoResponses.GetExclusionesObjetoResponse
import consumers.no_registral.exclusiones_objeto.infrastructure.dependency_injection.ExclusionesObjetoActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import scala.util.{Success, Try}

class GetStateExclusionesObjetoHandler (actor: ExclusionesObjetoActor) extends SyncQueryHandler[GetStateExclusionesObjeto]{
  override def handle(query: GetStateExclusionesObjeto): Try[GetStateExclusionesObjeto#ReturnType] = {
    val sender = actor.context.sender()

    val response = GetExclusionesObjetoResponse(
      Some(actor.state.registro.get),
      actor.state.fechaUltMod
    )
    log.info(s"[${actor.persistenceId}] GetState | $response")
    sender ! response
    Success(response)
  }

}
