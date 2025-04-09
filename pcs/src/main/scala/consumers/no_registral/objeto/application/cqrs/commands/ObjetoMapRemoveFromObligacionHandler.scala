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
    val sender = actor.context.sender()

    log.debug(
    f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
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
