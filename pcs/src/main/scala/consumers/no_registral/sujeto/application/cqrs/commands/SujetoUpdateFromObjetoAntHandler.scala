package consumers.no_registral.sujeto.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromObjetoAnt
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import scala.util.{Success, Try}

class SujetoUpdateFromObjetoAntHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromObjetoAnt] {
  override def handle(command: SujetoUpdateFromObjetoAnt): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val event = SujetoEvents.SujetoUpdatedFromObjetoAnt(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.saldoObjeto,
      command.saldoObligaciones,
      command.clasificacionObjeto
    )

    actor.persistEventTagsSujeto(event) { () =>
      actor.state += event

      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        actor.deleteMessages(actor.lastSequenceNr - 201)
      }
      actor.persistSnapshot() { _ =>
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
