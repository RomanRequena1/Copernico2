package consumers.no_registral.objeto.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import org.slf4j.LoggerFactory

import scala.util.{Success, Try}

class ObjetoUpdateFromObligacionHandler(actor: ObjetoActor)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromObligacion] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromObligacion
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = ObjetoUpdatedFromObligacion(
      actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.objetoId2,
      command.tipoObjeto,
      command.obligacionId,
      command.saldoObligacion,
      command.obligacionExenta,
      command.porcentajeExencion,
      command.idExterno
    )
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)



    actor.persistEvent(event) { () =>

      actor.state += event
      println("TREINTA 5 " + actor.state)
      //if (initialization != "true")
      //  actor.informParent(command, actor.state)
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
      if (actor.state.obnVencidas.contains(false)) {
        println("TREINTA 6.0 ")
        val newState = actor.state.copy(deuda30Objeto = false)
        println("TREINTA 7.0 " + newState)
        actor.informParentTreintaPorciento(command, actor.state)
        actor.persistSnapshot(event, newState) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }

      else {
        println("TREINTA 6.1 ")
        val newState = actor.state.copy(deuda30Objeto = true)
        println("TREINTA 7.1 ")
        actor.informParent(command, actor.state)
        actor.persistSnapshot(event, newState) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
      actor.persistSnapshot(event, actor.state){ () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
