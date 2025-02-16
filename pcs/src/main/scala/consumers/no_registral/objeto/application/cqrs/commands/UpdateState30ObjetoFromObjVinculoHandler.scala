package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.helper.{SendToSujeto, SendToSujeto1}
import consumers.no_registral.objeto.domain.ObjetoEvents.UpdatedState30ObjetoFromObjVinculo
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotentInternally

import scala.util.{Success, Try}

class UpdateState30ObjetoFromObjVinculoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObjetoCommands.UpdateState30ObjetoFromObjVinculo] {

  /**
   * Si el objeto tiene 30% manda mensaje a los objetos vinculados y si no manda mensaje a los objetos vinculados
   */
  override def handle(
      command: ObjetoCommands.UpdateState30ObjetoFromObjVinculo
  ): Try[Response.SuccessProcessing] = {
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val sender = actor.context.sender()

    val event = UpdatedState30ObjetoFromObjVinculo(
      //TODO: validar para que esta este If
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30ObjetoVinculo,
      command.exclusionObjetoVinculo
    )

    if (isIdempotentInternally(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond internally_idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-INT-" + command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        if (actor.state.eventCounter == eventCounterMax) {
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }

        if (actor.state.tiene30Objeto.equals(false)) {
          SendToSujeto(actor, requeriment, event)

        } else {
          SendToSujeto1(actor, requeriment, event)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
