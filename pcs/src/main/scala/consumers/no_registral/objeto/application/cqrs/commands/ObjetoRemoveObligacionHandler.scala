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
    val sender = actor.context.sender()
    val event = ObjetoRemovedObligacion(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.cuota
    )
    actor.persistEvent(event) { () =>
      actor.state += event
      println("CUMBIA O D  " + actor.state)
      if(!actor.state.isBaja){
        //actor.informParent(command, actor.state)
        //actor.persistSnapshot(event, actor.state)(() => ())
        if (actor.state.tiene30Objeto.equals(false)) {

          actor.informParentTreintaPorciento(command, actor.state)
          actor.persistSnapshot(event, actor.state) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }

        else {

          actor.informParent(command, actor.state)
          actor.persistSnapshot(event, actor.state) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }
      }


    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}