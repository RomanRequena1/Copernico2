package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemove
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import scala.util.{Success, Try}

class ObligacionRemoveHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionRemove] {
  override def handle(command: ObligacionRemove): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    val event =
      ObligacionEvents.ObligacionRemoved(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId,
        command.registro,
        command.cuota
      )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -obligacion- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )

      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEventTagsSujeto(event) { () =>
        actor.state += event
        actor.informRemoveToParent(command)
        actor.lastDeliveryId = command.deliveryId
        actor.deleteSnapshot(event) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }
  }
}

