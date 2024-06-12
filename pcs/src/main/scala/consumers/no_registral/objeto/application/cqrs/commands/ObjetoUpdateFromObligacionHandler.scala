package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.helper.SendObjetoToObjetoVinculo
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromObligacionHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromObligacion] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromObligacion
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"),None,None)

    val event = ObjetoUpdatedFromObligacion(
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
      command.couta
    )
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)

    implicit val ac: ActorSystem = actor.context.system
    val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

    actor.persistEvent(event) { () =>
      actor.state += event
      //if (initialization != "true")
      //  actor.informParent(command, actor.state)
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }

      SendObjetoToObjetoVinculo(vinculoActor, actor, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state.registro.getOrElse(obj_default).SOJ_ESTADO, requeriment)
//      actor.persistSnapshot(event, actor.state){ () =>
//        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//
//      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
