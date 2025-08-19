package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class DeleteObjectIfNoObligacionesHandler(actor: ObjetoActor, requirements: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.DeleteObjectIfNoObligaciones] {

  override def handle(command: ObjetoCommands.DeleteObjectIfNoObligaciones): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    // Verificar si el objeto ya fue eliminado
    if (actor.state.isDeleted) {
      log.warn(s"El objeto ${command.objetoId} ya fue eliminado previamente")
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      return Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }

    // Primero removemos la obligación del estado del objeto
    val eventRemove = ObjetoEvents.ObjetoRemovedObligacion(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.cuota
    )

    actor.persistEvent(eventRemove) { () =>
      actor.state += eventRemove

      // Verificar las obligaciones restantes DESPUÉS de remover
      val obligacionesRestantes = actor.state.obligacionesSaldo

      // Si no quedan obligaciones y el tipo es PPP o PM26, eliminar completamente el objeto
      if (obligacionesRestantes.isEmpty && (command.tipoObjeto == "PPP" || command.tipoObjeto == "PM26")) {

        log.info(s"Eliminando objeto ANT ${command.tipoObjeto} ${command.objetoId} - No quedan obligaciones")

        // Crear evento de eliminación completa
        val eventDelete = ObjetoEvents.ObjetoDeleted(
          actor.state.lastDeliveryIdByEvents + 1,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto
        )

        actor.persistEvent(eventDelete) { () =>
          actor.state += eventDelete

          // Informar al sujeto padre que el objeto fue eliminado
          actor.context.parent ! SujetoCommands.SujetoRemoveObjeto(
            command.deliveryId,
            command.sujetoId,
            command.objetoId,
            command.tipoObjeto
          )

          // Eliminar completamente el objeto de la base de datos
          actor.deleteObjetoObligacionesSnapshot(eventDelete, actor.state) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
            // Detener el actor después de la eliminación
            actor.context.stop(actor.self)
          }
        }
      } else {
        // Si no se elimina, actualizamos el snapshot con la obligación removida
        actor.persistSnapshot(eventRemove, actor.state) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}