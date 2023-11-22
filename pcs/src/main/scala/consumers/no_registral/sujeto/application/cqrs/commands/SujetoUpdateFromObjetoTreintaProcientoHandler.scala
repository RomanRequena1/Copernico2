package consumers.no_registral.sujeto.application.cqrs.commands

import akka.actor.ActorRef
import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObnTreintaProciento
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.helper.SendToObjeto
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromObjetoTreintaPorciento
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import scala.util.{Success, Try}

class SujetoUpdateFromObjetoTreintaProcientoHandler(actor: SujetoActor)
  extends SyncCommandHandler[SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento] {
  override def handle(
                       command: SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento
                     ): Try[Response.SuccessProcessing] = {
    val sender: ActorRef = actor.context.sender()
    val event = SujetoEvents.SujetoUpdatedFromObjetoTreintaPorciento(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.saldoObjeto,
      command.saldoObligaciones,
      command.clasificacionObjeto
    )
    //    val initialization: String = {
    //      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    //    }

    //    if (initialization != "true") {

    actor.persistEvent(event) { () =>
      actor.state += event
      val map2 = actor.state.objVencidas.filter( obj => obj._2._2.equals("TIPO2"))
      if (map2.values.forall(_ == (true, "TIPO2"))) { // todo tipo 2 - tipo 1 no afecta el 30 del sujeto - objeto no se ve afectado por el 30 del sujeto
        val newState = actor.state.copy(deuda30Sujeto = true)
        SendToObjeto(actor.state, newState, sender, actor.context.children, actor.context)
        actor.state = newState
      }
      else {
        val newState = actor.state.copy(deuda30Sujeto = false)
        SendToObjeto(actor.state, newState, sender, actor.context.children, actor.context)
        actor.state = newState
      }

      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }

      actor.persistSnapshot() { _ =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      }
    }
    //    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
