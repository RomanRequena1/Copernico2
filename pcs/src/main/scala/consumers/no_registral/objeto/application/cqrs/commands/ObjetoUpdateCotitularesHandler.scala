package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedCotitulares
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateCotitularesHandler(actor: ObjetoActor)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateCotitulares] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateCotitulares
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    val event = ObjetoUpdatedCotitulares(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.cotitulares
    )

    actor.persistEvent(event) { () =>
      actor.state += event
      actor.informParent(actor.state.lastDeliveryIdByEvents,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        actor.state)
      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
