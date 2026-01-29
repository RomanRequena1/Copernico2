package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.helper.SendObjetoToObjetoVinculo
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObnTreintaProciento
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
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
    val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"),Some("None"), Some("None"), Some("None"),None,None)

    println(s"[HANDLER-OBN30-ENTRY] deliveryId=${command.deliveryId}, sujetoId=${command.sujetoId}, " +
      s"objetoId=${command.objetoId}, obligacionId=${command.obligacionId}")

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

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
      command.cuota,
      command.dmnNumero,
      command.dmnDescripcion
    )

    implicit val ac: ActorSystem = actor.context.system
    val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

    actor.persistEvent(event) { () =>
      println(s"[HANDLER-OBN30-PERSISTED] deliveryId=${command.deliveryId}, objetoId=${command.objetoId} - " +
        s"Evento persistido, estado=${actor.state.registro.map(_.SOJ_ESTADO).getOrElse("NONE")}")
      actor.state += event
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 200))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        actor.deleteMessages(actor.lastSequenceNr - 201)
      }
      println(s"[HANDLER-OBN30-SENDING] deliveryId=${command.deliveryId}, objetoId=${command.objetoId} - " +
        s"Llamando SendObjetoToObjetoVinculo")
      SendObjetoToObjetoVinculo(
        vinculoActor,
        actor,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
        requeriment,
        command)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}