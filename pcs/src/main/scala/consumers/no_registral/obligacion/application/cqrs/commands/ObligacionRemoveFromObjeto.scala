package consumers.no_registral.obligacion.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemoveInfoFromObjeto
import consumers.no_registral.obligacion.application.helper.SendObligacionToObjeto
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotentInternally

import scala.util.{Success, Try}

class ObligacionRemoveFromObjeto(actor: ObligacionActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObligacionRemoveInfoFromObjeto] {
  override def handle(command: ObligacionRemoveInfoFromObjeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    val event =
      ObligacionEvents.ObligacionRemovedInfoFromObjeto(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId,
        actor.state.registro match {
          case Some(value) => value
          case None     => null
        },
        actor.state.registro match {
          case Some(value) => Some(value.BOB_CUOTA.getOrElse("0"))
          case None     => Some("0")
        })


    if (isIdempotentInternally(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond internally_idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-INT-" + command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        SendObligacionToObjeto(actor, requeriment, event)
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
