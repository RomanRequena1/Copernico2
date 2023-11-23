package consumers.no_registral.sujeto.application.cqrs.commands

import akka.Done
import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoSetBajaFromObjeto
import consumers.no_registral.sujeto.application.helper.SendToObjeto
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class SujetoSetBajaFromObjetoHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoSetBajaFromObjeto] {
  override def handle(command: SujetoSetBajaFromObjeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = SujetoEvents.SujetoBajaFromObjetoSet(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto
    )

    actor.persistEvent(event,Set("Sujeto")) { () =>
      actor.state += event
      val map2 = actor.state.objVencidas.filter(obj => obj._2._2.equals("TIPO2"))
      if (map2.values.forall(_._1)) { // todo tipo 2 - tipo 1 no afecta el 30 del sujeto - objeto no se ve afectado por el 30 del sujeto
        val newState = actor.state.copy(deuda30Sujeto = true)
        SendToObjeto(actor.state, newState, sender, actor.context.children, actor.context, event)
        actor.state = newState
      }
      else {
        val newState = actor.state.copy(deuda30Sujeto = false)
        SendToObjeto(actor.state, newState, sender, actor.context.children, actor.context, event)
        actor.state = newState
      }

      actor.persistSnapshot()(_ => ())
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
