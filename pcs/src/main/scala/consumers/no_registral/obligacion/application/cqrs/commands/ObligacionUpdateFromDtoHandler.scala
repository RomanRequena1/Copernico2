package consumers.no_registral.obligacion.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ListDetallesObligaciones, ObligacionExternalDto}
import consumers.no_registral.obligacion.application.helper.StateParcialObligacion
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import scala.util.{Success, Try}

class ObligacionUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionUpdateFromDto] {
  override def handle(command: ObligacionUpdateFromDto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    def getCCParams(evento: ObligacionExternalDto, estado: ObligacionExternalDto): ObligacionExternalDto = {
      StateParcialObligacion.stateParcialCC(evento, Some(estado))
    }

    def getObligacionFFF() = {
      val obligacionFFF = actor.state.registro match {
        case None => StateParcialObligacion.stateParcialCC(command.registro, None)
        case Some(value) => {
          getCCParams(command.registro, value)
        }
      }
      obligacionFFF
    }

    val stateParcialEnabled: String = Option(System.getenv("STATE_PARCIAL_OBLIGACION_TRI")).getOrElse("OFF")

    val event = ObligacionUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      stateParcialEnabled.equals("ON") match {
        case true => getObligacionFFF()
        case false => command.registro
      },
      command.detallesObligacion,
      command.detallesCaracteristicas,
      command.detallesSupresiones,
      command.isAdheridoDebito,
      command.cuota,
      command.resultDmn,
    )

    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -obligacion- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )

      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        actor.state += event

        // Safe check para tiene30Obligaciones
        val tiene30 = event.registro.BOB_OTROS_ATRIBUTOS
          .flatMap(_.BOB_DETALLES.headOption)
          .flatMap(_.tiene30Obligaciones)

        tiene30 match {
          case Some(true) => actor.informParent(command)
          case Some(false) => actor.informParentTreintaProciento(event)
          case None =>
            // Esto NO debería pasar para TRI, pero por si acaso
            log.warn(s"Obligación TRI sin tiene30Obligaciones: ${command.obligacionId}")
            actor.informParent(command)
        }

        if (actor.state.eventCounter == eventCounterMax) {
          actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 2))
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
        actor.lastDeliveryId = command.registro.EV_ID
        actor.persistSnapshot(event) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}