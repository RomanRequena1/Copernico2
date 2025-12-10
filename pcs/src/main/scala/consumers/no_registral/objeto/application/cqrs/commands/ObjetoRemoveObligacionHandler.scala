package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.helper.SendObjetoToObjetoVinculo
import consumers.no_registral.objeto.domain.ObjetoEvents._
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.concurrent.ExecutionContext
import scala.util.{Success, Try}

class ObjetoRemoveObligacionHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.ObjetoRemoveObligacion] {

  override def handle(
                       command: ObjetoCommands.ObjetoRemoveObligacion
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"), None, None)

    val event = ObjetoRemovedObligacion(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.cuota,
      command.dmnNumero,
      command.dmnDescripcion
    )

    implicit val ac: ActorSystem = actor.context.system
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
    val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

    actor.persistEvent(event) { () =>
      actor.state += event

      log.info(s"[PAGO-DEBUG-2] Evento persistido - objetoId=${command.objetoId}, tiene30Objeto=${actor.state.tiene30Objeto}")

      actor.persistSnapshot(event, actor.state) { () =>
        if (!actor.state.isBaja && actor.state.registro.isDefined) {
          SendObjetoToObjetoVinculo(
            vinculoActor,
            actor,
            command.sujetoId,
            command.objetoId,
            command.tipoObjeto,
            actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
            requeriment,
            command
          )
        }
        else {
          log.warn(s"[PAGO-DEBUG-3-SKIP] NO se envió a vinculo - isBaja=${actor.state.isBaja}, registroDefined=${actor.state.registro.isDefined}")
        }
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}