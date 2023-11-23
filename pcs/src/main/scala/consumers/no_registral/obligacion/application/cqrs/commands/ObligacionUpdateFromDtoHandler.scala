package consumers.no_registral.obligacion.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import scala.util.{Success, Try}

class ObligacionUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionUpdateFromDto] {
  override def handle(command: ObligacionUpdateFromDto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = ObligacionUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.registro,
      command.detallesObligacion,
      command.isAdheridoDebito,
      command.cuota
    )
    // check whether we are in initialization mode or not
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(s"[${actor.name} | ${actor.persistenceId}] -obligacion- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)

      // Informs that operation has been ignored */
      //todo check if this is desirable, why? signal the sender??

      // In this case the sender is "EL OBJETO"
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        println("TREINTA 2 " + actor.state)
        if (!(initialization == "true" && command.registro.BOB_ESTADO.contains("ADMINISTRATIVA"))) {
          //actor.informParent(command)
        }
        if (event.registro.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head.BAND_30.get.equals(true)) {
          println("TREINTA 3.0 ")
          actor.informParent(command)
        }

        else {
          println("TREINTA 3.1 ")
          actor.informParentTreintaProciento(event)
        }

        if (actor.state.eventCounter == eventCounterMax) {
          actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 2))
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
        actor.lastDeliveryId = command.registro.EV_ID
        actor.persistSnapshot() { () =>
          //println("CUMBIA path sender" + sender.path)
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
