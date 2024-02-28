package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.RemovedObjetoFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoMapRemoveFromObligacionHandler(actor: ObjetoActor)
  extends SyncCommandHandler[ObjetoCommands.RemoveObjetoFromObligacion] {
  override def handle(
                       command: ObjetoCommands.RemoveObjetoFromObligacion
                     ): Try[Response.SuccessProcessing] = {

    log.error("Llego al handler del removemap")

    val event = RemovedObjetoFromObligacion(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.cuota
    )

    actor.persistEvent(event) { () =>
      actor.state += event
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
