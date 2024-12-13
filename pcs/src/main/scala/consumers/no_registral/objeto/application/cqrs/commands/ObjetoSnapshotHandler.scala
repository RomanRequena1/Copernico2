package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.domain.ObligacionEvents
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotentInternally

import scala.util.{Success, Try}

class ObjetoSnapshotHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoSnapshot] {
  override def handle(
      command: ObjetoCommands.ObjetoSnapshot
  ): Try[Response.SuccessProcessing] = {
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val event = ObjetoSnapshotPersisted(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.objetoId2,
      command.tipoObjeto,
      command.saldo,
      command.cotitulares,
      command.tags,
      command.sujetoResponsable,
      actor.state.porcentajeResponsabilidad,
      actor.state.registro,
      command.obligacionesSaldo,
      actor.state.cuotas,
      actor.state.clasificacionObjeto,
      operacion = ObligacionEvents.operaciones("Upsert"),
      command.idExterno,
      Some(actor.state.tiene30Objeto),
      actor.state.aplicarDescuento,
      actor.state.resulDmn.getOrElse(0),
      actor.state.exclusionObjeto,
      Some(actor.state.tiene30ObjetoVinculo)
    )
    val consolidatedState = actor.state + event
    val sender = actor.context.sender()

    if (isIdempotentInternally(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond internally_idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-INT-" + command.aggregateRoot, command.deliveryId)

    } else {
    actor.persistSnapshot(event, consolidatedState) { () =>
      actor.state = consolidatedState
      actor.informParent(actor.state.lastDeliveryIdByEvents, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state)
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
      }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
