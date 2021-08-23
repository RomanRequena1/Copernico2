package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents._
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoRemoveObligacionHandler(actor: ObjetoActor)
    extends SyncCommandHandler[ObjetoCommands.ObjetoRemoveObligacion] {
  override def handle(
      command: ObjetoCommands.ObjetoRemoveObligacion
  ): Try[Response.SuccessProcessing] = {

    val event = ObjetoRemovedObligacion(
      // TODO remember to add the deliveryId command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId
    )
    actor.persistEvent(event) { () =>
      actor.state += event
      actor.informParent(command, actor.state)
      actor.persistSnapshot(event, actor.state)(() => ())
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
