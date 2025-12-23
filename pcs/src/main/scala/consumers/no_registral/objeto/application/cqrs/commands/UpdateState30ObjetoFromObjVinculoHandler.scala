package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral. objeto.application.helper.{SendToSujeto, SendToSujeto1}
import consumers.no_registral. objeto.domain.ObjetoEvents.UpdatedState30ObjetoFromObjVinculo
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class UpdateState30ObjetoFromObjVinculoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.UpdateState30ObjetoFromObjVinculo] {

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
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30ObjetoVinculo,
      command.exclusionObjetoVinculo,
    )

    actor.persistEvent(event) { () =>
      actor.state += event
      actor.persistSnapshot(event, actor.state) { () =>

        val tiene30ObjetoFinal = if (!command.tiene30ObjetoVinculo && actor.state.obligaciones.isEmpty) {
          false
        } else if (command.tiene30ObjetoVinculo && actor.state.obligaciones.isEmpty) {
          true
        } else {
          actor.state.tiene30Objeto
        }

        actor.state = actor.state.copy(tiene30Objeto = tiene30ObjetoFinal)

        // Actualizar el DMN del objeto si viene del vinculo
        // Esto permite que objetos sin obligaciones propias usen el DMN de cotitulares
        val (dmnNumeroFinal, dmnDescripcionFinal) = {
          if (actor.state.dmnNumero.isDefined) {
            (actor.state.dmnNumero, actor.state.dmnDescripcion)
          } else if (command.dmnNumero.isDefined) {
            (command.dmnNumero, command.dmnDescripcion)
          } else {
            (None, None)
          }
        }

        actor.state = actor.state.copy(
          dmnNumero = dmnNumeroFinal,
          dmnDescripcion = dmnDescripcionFinal
        )

        actor.persistSnapshot(event, actor.state) { () =>
          if (tiene30ObjetoFinal) {
            SendToSujeto1(actor, requeriment, event, Some(command.deliveryId))
          } else {
            SendToSujeto(actor, requeriment, event, Some(command.deliveryId))
          }
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }

      if (actor.state.eventCounter == eventCounterMax) {
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}