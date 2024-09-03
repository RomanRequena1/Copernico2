package consumers.no_registral.objeto.application.cqrs.queries

import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetStateObjeto
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetObjetoResponse
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import scala.util.{Success, Try}

class GetStateObjetoHandler(actor: ObjetoActor) extends SyncQueryHandler[GetStateObjeto] {
  override def handle(query: GetStateObjeto): Try[GetStateObjeto#ReturnType] = {
    val sender = actor.context.sender()

    val response = GetObjetoResponse(
      lastDeliveryIdByEvents = actor.state.lastDeliveryIdByEvents,
      saldo = actor.state.saldo,
      tags = actor.state.tags,
      obligaciones = actor.state.obligaciones,
      sujetos = actor.state.sujetos,
      sujetoResponsable = actor.state.sujetoResponsable,
      fechaUltMod = actor.state.fechaUltMod,
      registro = actor.state.registro,
      exenciones = actor.state.exenciones,
      bandTipo = actor.state.clasificacionObjeto,
      treinta = actor.state.tiene30Objeto,
      treintaFinal = actor.state.aplicarDescuento.getOrElse(true)
    )
    log.info(s"[${actor.persistenceId}] GetState | $response")
    sender ! response
    Success(response)
  }
}
