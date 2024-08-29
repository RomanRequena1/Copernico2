package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class ObjetoUpdateFromAntHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromAnt] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromAnt
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val event = ObjetoEvents.ObjetoUpdatedFromAnt(
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito
    )
// FIXME: Chequear si debemos sumar algun comportamiento del ObjetoUpdateFromTriHandler
    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParentAnt(actor.state.lastDeliveryIdByEvents,
                              command.sujetoId,
                              command.objetoId,
                              command.tipoObjeto,
                              actor.state)
        actor.persistSnapshot(event, actor.state) { () => () }
      }
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
