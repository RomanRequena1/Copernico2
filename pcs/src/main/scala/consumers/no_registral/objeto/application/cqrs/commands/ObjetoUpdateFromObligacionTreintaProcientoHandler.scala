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
      println("STATE OBJ FROM OBN30: " + actor.state)
      actor.state += event
      //if (initialization != "true")
      //  actor.informParent(command, actor.state)
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }

      if(actor.state.tiene30Objeto.equals(false)){

        actor.informParentTreintaPorciento(command, actor.state)
       // actor.persistSnapshot(event, actor.state) { () =>
          //sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        //}
      }

      else {

        actor.informParent(command, actor.state)
       // actor.persistSnapshot(event, actor.state) { () =>
        //  sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
       // }
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

