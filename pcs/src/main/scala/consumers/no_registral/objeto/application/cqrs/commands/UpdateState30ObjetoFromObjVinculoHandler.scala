package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.application.helper.{SendToSujeto, SendToSujeto1}
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.{ObjetoUpdatedFromObligacion, UpdatedState30ObjetoFromObjVinculo}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class UpdateState30ObjetoFromObjVinculoHandler(actor: ObjetoActor,requeriment: MonitoringAndMessageProducer) extends SyncCommandHandler[ObjetoCommands.UpdateState30ObjetoFromObjVinculo] {

  /**
   * Si el objeto tiene 30% manda mensaje a los objetos vinculados y si no manda mensaje a los objetos vinculados
   */
  override def handle(
                       command: ObjetoCommands.UpdateState30ObjetoFromObjVinculo
                     ): Try[Response.SuccessProcessing] = {
    val event = UpdatedState30ObjetoFromObjVinculo(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30ObjetoVinculo
    )




    println("LLEGO A UpdateState30ObjetoFromObjVinculoHandler" + command)
    actor.persistEvent(event) { () =>
      actor.state += event
      if (actor.state.eventCounter == eventCounterMax) {
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }

      if(actor.state.tiene30Objeto.equals(false)) {
        println("LLEGO A UpdateState30ObjetoFromObjVinculoHandler false" + command)

        SendToSujeto(actor, requeriment, event)

      } else {
        println("LLEGO A UpdateState30ObjetoFromObjVinculoHandler true" + command)
        SendToSujeto1(actor, requeriment, event)
      }




    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}