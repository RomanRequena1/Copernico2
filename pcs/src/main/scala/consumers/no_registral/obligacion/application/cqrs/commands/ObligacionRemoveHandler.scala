package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemove
import consumers.no_registral.obligacion.application.entities.{ObligacionExternalDto, ObligacionesAnt}
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import scala.util.{Success, Try}

class ObligacionRemoveHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionRemove] {
  override def handle(command: ObligacionRemove): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    // Función helper para verificar si es una baja por pago (RULE_NUMBER = -1)
    def isPagoObligacion(registro: ObligacionExternalDto): Boolean = {
      registro match {
        case obligacionAnt: ObligacionesAnt =>
          obligacionAnt.BOB_OTROS_ATRIBUTOS.exists { listaDetalles =>
            listaDetalles.BOB_DETALLES.exists { detalle =>
              detalle.RULE_NUMBER.exists(_ == "-1")
            }
          }
        case _ => false
      }
    }

    val event =
      ObligacionEvents.ObligacionRemoved(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId,
        command.registro,
        command.cuota
      )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -obligacion- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )

      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)
      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEventTagsSujeto(event) { () =>
        actor.state += event

        // Verificar si es una baja por pago y si el tipo de objeto es PPP o PM26
        val esBajaPorPago = isPagoObligacion(command.registro)
        val tipoObjetoValido = command.tipoObjeto == "PPP" || command.tipoObjeto == "PM26"

        if (esBajaPorPago && tipoObjetoValido) {
          // Enviar comando al objeto padre para verificar si debe eliminarse
          actor.context.parent ! ObjetoCommands.DeleteObjectIfNoObligaciones(
            command.deliveryId,
            command.sujetoId,
            command.objetoId,
            command.tipoObjeto,
            command.obligacionId,
            command.cuota
          )
        } else {
          // Si no es pago o no es PPP/PM26, ejecutar flujo normal
          actor.informRemoveToParent(command)
        }

        actor.lastDeliveryId = command.deliveryId
        actor.deleteSnapshot(event) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }
  }
}