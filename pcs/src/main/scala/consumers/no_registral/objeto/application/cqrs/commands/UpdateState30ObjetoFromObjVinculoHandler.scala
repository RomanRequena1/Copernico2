package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class TestHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.CommandTest] {
  override def handle(
                       command: ObjetoCommands.CommandTest
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()


    println("Llego TestHandler " + command)
    val event = ObjetoUpdatedFromObligacion(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      None,
      command.tipoObjeto,
      "None",
      1.1,
      false,
      None,
      None,
      None

    )

    actor.persistSnapshot(event, actor.state) { () =>
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
