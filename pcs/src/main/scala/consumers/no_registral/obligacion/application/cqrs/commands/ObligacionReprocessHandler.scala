package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionReprocess
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObligacionReprocessHandler(actor: ObligacionActor)
  extends SyncCommandHandler[ObligacionReprocess] {

  override def handle(command: ObligacionReprocess): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    actor.state.registro match {
      case Some(registro) =>
        val rn: String = registro.BOB_OTROS_ATRIBUTOS match {
          case Some(value) => value.BOB_DETALLES.head.RULE_NUMBER.getOrElse("1")
          case None => "1"
        }

        if (rn.equals("-1") || rn.equals("-2")) {
          val event = consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionRemoved(
            deliveryId = actor.state.lastDeliveryIdByEvents,
            sujetoId =  command.sujetoId,
            objetoId = command.objetoId,
            tipoObjeto = command.tipoObjeto,
            obligacionId = command.obligacionId,
            registro = registro,
            cuota = registro.BOB_CUOTA
          )
        } else {
          val event = consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto(
            actor.state.lastDeliveryIdByEvents,
            command.sujetoId,
            command.objetoId,
            command.tipoObjeto,
            command.obligacionId,
            registro,
            actor.state.detallesObligacion,
            actor.state.detallesSupresiones.getOrElse(Seq.empty),
            Some(actor.state.isAdheridoDebito),
            registro.BOB_CUOTA,
            actor.state.resultDmn
          )
        }
        println(s"Reprocesado evento para obligación: ${ObligacionMessageRoots(command.sujetoId, command.objetoId, command.tipoObjeto, command.obligacionId)}")

//        actor.persistEventTagsSujeto(event) { () => ()
////          log.info(s"Reprocesado evento para obligación: ${command.obligacionId}")
////          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//        }

      case None => ()
//        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}