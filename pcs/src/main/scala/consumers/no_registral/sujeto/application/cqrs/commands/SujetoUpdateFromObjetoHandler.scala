package consumers.no_registral.sujeto.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromObjeto
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class SujetoUpdateFromObjetoHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromObjeto] {
  override def handle(command: SujetoUpdateFromObjeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = SujetoEvents.SujetoUpdatedFromObjeto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.saldoObjeto,
      command.saldoObligaciones
    )
//    val initialization: String = {
//      Try(System.getenv("INITIALIZATION")).getOrElse(null)
//    }

//    if (initialization != "true") {

    actor.persistEvent(event) { () =>
      actor.state += event
      actor.persistSnapshot(){ _ =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        if (actor.state.eventCounter > eventCounterMax) {
          //actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 51))
          println("Snapshot suj -" + command.deliveryId + "Event counter: " + actor.state.eventCounter + " SeqNr: " + actor.lastSequenceNr)
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
      }
    }
//    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
