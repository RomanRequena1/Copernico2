package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotentInternally

import scala.util.{Success, Try}

class ObjetoTagAddHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoTagAdd] {
  override def handle(
      command: ObjetoCommands.ObjetoTagAdd
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val event = ObjetoEvents.ObjetoTagAdded(command.deliveryId,
                                            command.sujetoId,
                                            command.objetoId,
                                            command.tipoObjeto,
                                            command.tag)
    if (isIdempotentInternally(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond internally_idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-INT-" + command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.persistSnapshot(event, actor.state) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
