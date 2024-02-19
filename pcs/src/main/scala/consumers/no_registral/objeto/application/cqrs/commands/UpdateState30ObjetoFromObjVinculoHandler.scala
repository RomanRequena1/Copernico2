package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.{ObjetoUpdatedFromObligacion, UpdatedState30ObjetoFromObjVinculo}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class UpdateState30ObjetoFromObjVinculoHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.UpdateState30ObjetoFromObjVinculo] {

  /**
   * Si el objeto tiene 30% manda mensaje a los objetos vinculados y si no manda mensaje a los objetos vinculados
   */
  override def handle(
                       command: ObjetoCommands.UpdateState30ObjetoFromObjVinculo
                     ): Try[Response.SuccessProcessing] = {
    println("Llego UpdateState30ObjetoFromObjVinculoHandler " + command + " - " + command.tiene30ObjetoVinculo)
    val event = UpdatedState30ObjetoFromObjVinculo(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30ObjetoVinculo
    )
    println("EVENTO UPDATESTATE30OBJETOFROMOBJVINCULO : "+event)
    actor.persistEvent(event) { () =>
      actor.state += event
      if (actor.state.eventCounter == eventCounterMax) {
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
      println("CUMBIA "  + command + " - " + actor.state.tiene30Objeto)




      if(actor.state.tiene30Objeto.equals(false)) {
        println("ENTRO AL IF TIENE30 FALSE UPDATESTATE30OBJFROMOBJVINCULO")
        actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state)
      } else
        println("ENTRO AL ELSE TIENE30 TRUE UPDATESTATE30OBJFROMOBJVINCULO")
        actor.informParent(actor.state.lastDeliveryIdByEvents, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}