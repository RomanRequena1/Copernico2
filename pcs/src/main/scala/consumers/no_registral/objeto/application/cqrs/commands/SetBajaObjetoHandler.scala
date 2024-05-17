package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.helper.{SendObjetoToObjetoVinculo, SendToObligaciones}
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class SetBajaObjetoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer) extends SyncCommandHandler[ObjetoCommands.SetBajaObjeto] {
  override def handle(
      command: ObjetoCommands.SetBajaObjeto
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = ObjetoEvents.ObjetoBajaSet(
      actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable
    )
    if(command.deliveryId.signum < 0 || !isIdempotent(command, actor.state.lastDeliveryIdByEvents)){
      implicit val ac: ActorSystem = actor.context.system
      val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informBajaToParent(command)
        actor.deleteSnapshot(event, actor.state) { () => //todo revisar si el baja es con estado o con el saldo de todas obligaciones en 0 o ambas?
          actor.deleteObjetoObligacionesSnapshot(event, actor.state) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }
        SendToObligaciones(actor)
        SendObjetoToObjetoVinculo(Obje,actor, command.sujetoId, command.objetoId, command.tipoObjeto, command.registro.SOJ_ESTADO, requeriment)
      }
    }

    else if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
