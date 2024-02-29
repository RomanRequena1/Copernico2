package consumers.no_registral.obligacion.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemoveInfoFromObjeto
import consumers.no_registral.obligacion.application.helper.SendObligacionToObjeto
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObligacionRemoveFromObjeto(actor: ObligacionActor, requeriment: MonitoringAndMessageProducer) extends SyncCommandHandler[ObligacionRemoveInfoFromObjeto] {
  override def handle(command: ObligacionRemoveInfoFromObjeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()


    val event =
      ObligacionEvents.ObligacionRemovedInfoFromObjeto(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId,
        actor.state.registro.get,
        actor.state.registro.get.BOB_CUOTA,
      )

      actor.persistEvent(event) { () =>
        SendObligacionToObjeto(actor, requeriment, event)
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))

  }
}
