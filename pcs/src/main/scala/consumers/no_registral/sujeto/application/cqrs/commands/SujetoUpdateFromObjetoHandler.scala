package consumers.no_registral.sujeto.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromObjeto
import consumers.no_registral.sujeto.application.helper.SendToObjeto
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
      command.saldoObligaciones,
      command.clasificacionObjeto
    )
//    val initialization: String = {
//      Try(System.getenv("INITIALIZATION")).getOrElse(null)
//    }

//    if (initialization != "true") {

    actor.persistEvent(event) { () =>

      actor.state += event
      println("TREINTA 8 " + event.clasificacionObjeto)
      println("TREINTA 8 " + actor.state)
      val map2 = actor.state.objVencidas.filter( obj => obj._2._2.equals("TIPO2"))
      if (map2.values.forall(_._1)) {
        println("TREINTA 9.0 " + map2)
        val newState = actor.state.copy(deuda30Sujeto = true)
        println("TREINTA 10.0 " + newState)
        SendToObjeto(actor.state, newState, sender, actor.context.children, actor.context, event)
        actor.state = newState
      }
      else {
        println("TREINTA 9.1 ")
        val newState = actor.state.copy(deuda30Sujeto = false)
        println("TREINTA 10.1 ")
        SendToObjeto(actor.state, newState, sender, actor.context.children, actor.context, event)
        actor.state = newState
      }
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
      actor.persistSnapshot(){ _ =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
//    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
