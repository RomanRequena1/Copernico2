package consumers.no_registral.objeto.application.cqrs.queries

import consumers.no_registral.objeto.application.entities.ObjetoQueries.{GetAllObnObjeto, GetStateObjeto}
import consumers.no_registral.objeto.application.entities.ObjetoResponses
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetAllObnResponse, GetObjetoResponse, Obligacion}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler
import serialization.CbroSerialization

import scala.util.{Success, Try}

class GetAllObnObjetoHandler(actor: ObjetoActor) extends SyncQueryHandler[GetAllObnObjeto] {
  override def handle(query: GetAllObnObjeto): Try[GetAllObnObjeto#ReturnType] = {
    val sender = actor.context.sender()


    GetAllObnResponse(
      objetoId = query.objetoId,
      objetoTipo = query.tipoObjeto,
      saldo = actor.state.saldo,
      obligaciones = )



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
