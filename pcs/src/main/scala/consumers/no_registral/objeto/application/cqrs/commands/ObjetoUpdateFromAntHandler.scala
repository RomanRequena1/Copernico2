package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetoExternalDto}
import consumers.no_registral.objeto.application.helper.StateParcialObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import java.time.LocalDateTime
import scala.util.{Success, Try}

class ObjetoUpdateFromAntHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromAnt] {
  override def handle(
  command: ObjetoCommands.ObjetoUpdateFromAnt
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
    f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    def getCCParams(evento: ObjetoExternalDto, estado: ObjetoExternalDto): ObjetoExternalDto = {
      StateParcialObjeto.stateParcialCC(evento, Some(estado))
    }
    def getObjetoFFF() = {
      val objetoFFF = actor.state.registro match {
        case None => StateParcialObjeto.stateParcialCC(command.registro, None)
        case Some(value) => getCCParams(command.registro,value)
      }
      objetoFFF
    }

    val stateParcialEnabled: String = Option(System.getenv("STATE_PARCIAL_OBJETO_ANT")).getOrElse("OFF")

    val event = ObjetoEvents.ObjetoUpdatedFromAnt(
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      stateParcialEnabled.equals("ON") match {
        case true => getObjetoFFF()
        case false => command.registro
      },
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito
    )

    // FIXME: Chequear si debemos sumar algun comportamiento del ObjetoUpdateFromTriHandler
    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParentAnt(actor.state.lastDeliveryIdByEvents,
                              command.sujetoId,
                              command.objetoId,
                              command.tipoObjeto,
                              actor.state)
        actor.persistSnapshot(event, actor.state) { () =>
          ()
        }
      }
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
