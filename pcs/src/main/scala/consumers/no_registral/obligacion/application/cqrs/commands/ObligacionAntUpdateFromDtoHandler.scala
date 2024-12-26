package consumers.no_registral.obligacion.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionAntUpdateFromDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.helper.StateParcialObligacion
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionAntUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import java.time.LocalDateTime
import scala.util.{Success, Try}

class ObligacionAntUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionAntUpdateFromDto] {
  override def handle(command: ObligacionAntUpdateFromDto): Try[Response.SuccessProcessing] = {
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

    def getObligacionAntFFF() = {
      val obligacionFFF = actor.state.registro match {
        case None => StateParcialObligacion.stateParcialCC(command.registro, None)
        case Some(value) => {
          getCCParams(command.registro, value)
        }
      }
      obligacionFFF
    }

    val event = ObligacionAntUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      getObligacionAntFFF(),
      command.detallesObligacion,
      command.detallesSupresiones,
      command.isAdheridoDebito,
      command.cuota
    )
    // check whether we are in initialization mode or not
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(
        s"[${actor.name} | ${actor.persistenceId}] -obligacionAnt- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        if (!(initialization == "true" && command.registro.BOB_ESTADO.contains("ADMINISTRATIVA"))) {
          //actor.informParent(command)
        }
        actor.informParent(command)

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
