package consumers.no_registral.obligacion.application.cqrs.queries

import consumers.no_registral.obligacion.application.entities.ObligacionResponses.{GetMiniObligacionResponse, GetObligacionResponse}
import consumers.no_registral.obligacion.application.entities.{ObligacionQueries, ObligacionResponses}
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import java.time.ZonedDateTime
import scala.util.{Success, Try}

class ObligacionGetMiniStateHandler(actor: ObligacionActor) extends SyncQueryHandler[ObligacionQueries.GetMiniStateObligacion] {

  override def handle(
      query: ObligacionQueries.GetMiniStateObligacion
  ): Try[ObligacionResponses.GetMiniObligacionResponse] = {
    val sender = actor.context.sender()

    val response = GetMiniObligacionResponse(
      query.obligacionId,
      actor.state.saldo,
      actor.state.registro.get.BOB_VENCIMIENTO,
      actor.state.registro.get.BOB_ESTADO,
      actor.state.registro.get.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head.tiene30Obligaciones,
      actor.state.registro
    )
    import java.time.format.DateTimeFormatter
    val time = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(ZonedDateTime.now())

//    log.info(s"[${actor.persistenceId}] GetState [${time.toString}]| $response")

    sender ! response
    Success(response)
  }
}
