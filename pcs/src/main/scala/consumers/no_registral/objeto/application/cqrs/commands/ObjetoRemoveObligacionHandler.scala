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

import scala.util.{Success, Try}

class ObjetoRemoveObligacionHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.ObjetoRemoveObligacion] {
  override def handle(
                       command: ObjetoCommands.ObjetoRemoveObligacion
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val obj_default: ObjetosTri = ObjetosTri(Some("None"),0,"None","None","None",Some("None"),Some("None"),Some("None"),None,None,Some("None"),None,Some(0),Some("None"),Some(0),Some("None"),Some("None"),Some("None"),Some("None"))

    val event = ObjetoRemovedObligacion(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.cuota
    )
    implicit val ac: ActorSystem = actor.context.system
    val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
    actor.persistEvent(event) { () =>
      actor.state += event
      if(!actor.state.isBaja){
        //actor.informParent(command, actor.state)
        //actor.persistSnapshot(event, actor.state)(() => ())
        SendObjetoToObjetoVinculo(Obje,actor, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state.registro.getOrElse(obj_default).SOJ_ESTADO, requeriment)
      }


    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}