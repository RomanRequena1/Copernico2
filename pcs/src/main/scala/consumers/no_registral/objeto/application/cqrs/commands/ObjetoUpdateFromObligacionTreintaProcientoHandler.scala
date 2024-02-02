package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.helper.SendObjetoToObjetoVinculo
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObnTreintaProciento
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromObligacionTreintaProcientoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento] {
  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val obj_default: ObjetosTri = ObjetosTri(Some("None"),0,"None","None","None",Some("None"),Some("None"),Some("None"),None,None,Some("None"),None,Some(0),Some("None"),Some(0),Some("None"),Some("None"),Some("None"),Some("None"))

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
      actor.state += event
      //if (initialization != "true")
      //  actor.informParent(command, actor.state)
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }

      SendObjetoToObjetoVinculo(actor, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state.registro.getOrElse(obj_default).SOJ_ESTADO, requeriment)

    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

