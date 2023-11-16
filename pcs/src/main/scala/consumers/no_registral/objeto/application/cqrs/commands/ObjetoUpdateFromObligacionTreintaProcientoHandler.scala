package consumers.no_registral.objeto.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObnTreintaProciento
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromObligacionTreintaProcientoHandler(actor: ObjetoActor)
  extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento] {
  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    println("CUMBIA "  + actor.state.lastDeliveryIdByEvents)
    val event = ObjetoUpdatedFromObnTreintaProciento(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.objetoId2,
      command.tipoObjeto,
      command.obligacionId,
      command.saldoObligacion,
      command.obligacionExenta,
      command.porcentajeExencion,
      command.idExterno,
      command.cuota
    )
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)

    actor.persistEvent(event) { () =>
      actor.state += event
      println("STATE OBJETO UPDATE 30 -> " + actor.state.obnVencidas)
      if (initialization != "true")
        actor.informParent(command, actor.state)
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }

      if(actor.state.obnVencidas.contains(false)){

        val newState = actor.state.copy(band30 = false)
        actor.persistSnapshot(event, newState) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }

      else {
        val newState = actor.state.copy(band30 = true)
        actor.persistSnapshot(event, newState) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

