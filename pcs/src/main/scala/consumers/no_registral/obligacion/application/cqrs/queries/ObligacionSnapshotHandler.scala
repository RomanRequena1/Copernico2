package consumers.no_registral.obligacion.application.cqrs.queries

import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.obligacion.application.entities.{ObligacionQueries, ObligacionResponses}
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import java.time.ZonedDateTime
import scala.util.{Success, Try}

class ObligacionSnapshotHandler(actor: ObligacionActor)
    extends SyncQueryHandler[ObligacionQueries.GetSnapshotObligacion] {

  override def handle(
      query: ObligacionQueries.GetSnapshotObligacion
  ): Try[ObligacionResponses.GetObligacionResponse] = {
    val sender = actor.context.sender()

    val response = GetObligacionResponse(
      actor.state.saldo,
      actor.state.fechaUltMod,
      actor.state.exenta,
      actor.state.porcentajeExencion,
      actor.state.registro,
      actor.state.lastDeliveryIdByEvents,
      actor.state.detallesObligacion,
      actor.state.detallesSupresiones,
      actor.state.juicioId,
      actor.state.isAdheridoDebito,
      actor.state.eventCounter,
      actor.state.idExterno,
      actor.state.resultDmn
    )
    import java.time.format.DateTimeFormatter
    val time = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(ZonedDateTime.now())

    actor.saveSnapshot(actor.state)

    log.info(s"[${actor.persistenceId}] GetState [${time.toString}]| $response")

    sender ! response
    Success(response)
  }
}
