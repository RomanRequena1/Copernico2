package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents
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


    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
