package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo
import design_principles.actor_model.mechanism.DeliveryIdManagement._
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromAntHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromAnt] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromAnt
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val event = ObjetoEvents.ObjetoUpdatedFromAnt(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro
    )
    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

    } else {
      //val resultDMN = DMNTreintaPorcientoTipo.dmn(command)

      // because ObjetoNovedadCotitularidad, the event processor, needs this event to publish AddCotitular
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParent(actor.state.lastDeliveryIdByEvents, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state)
//        if (actor.state.eventCounter == eventCounterMax) {
//          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
//        }
//        resultDMN
//          .fold(e => {
//            println("ERROR DMN OBJETO: " + e)
//          },
//            {
//              case d if d.value.equals("2") =>
//                val newState = actor.state.copy(clasificacionObjeto = "TIPO2")
//                actor.persistSnapshot(event, newState) { () =>
//                  sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//                }
//              case _ =>
//                val newState = actor.state.copy(clasificacionObjeto = "TIPO1")
//                actor.persistSnapshot(event, newState) { () =>
//                  sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//                }
//            })
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
