package consumers.no_registral.exclusiones_objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoCommands
import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoEvents
import consumers.no_registral.exclusiones_objeto.infrastructure.dependency_injection.ExclusionesObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}
class ExclusionesObjetoUpdateFromDtoHandler (actor: ExclusionesObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto] {
  override def handle(
                       command: ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto(
      command.deliveryId,
      command.objetoId,
      command.registro
    )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))

  }
}
