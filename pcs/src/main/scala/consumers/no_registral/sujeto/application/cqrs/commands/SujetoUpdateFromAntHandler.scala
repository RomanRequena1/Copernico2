package consumers.no_registral.sujeto.application.cqrs.commands

import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromAnt
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromAnt
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class SujetoUpdateFromAntHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromAnt] {
  override def handle(command: SujetoUpdateFromAnt): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val event = SujetoUpdatedFromAnt(command.deliveryId, command.sujetoId, command.registro)

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.name} | ${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)
    } else {
      actor.persistEvent(event,Set("Sujeto")) { () =>
        actor.state += event
        actor.persistSnapshot() { _ =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }

}
