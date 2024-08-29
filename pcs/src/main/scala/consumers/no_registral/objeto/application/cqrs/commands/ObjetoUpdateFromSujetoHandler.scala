package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromSujeto
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromSujetoHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromSujeto] {
  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromSujeto
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = ObjetoUpdatedFromSujeto(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto
    )

    actor.state += event
   // val exclusionObjeto = QueryExclusionObjeto(command.objetoId)
    val obj_default = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"),None,None)
    val result = DMNTreintaPorcientoFinal.calcularDmnFinal(DmnFinal(command.exclusionSUjeto,
      actor.state.exclusionObjeto,
      actor.state.clasificacionObjeto,
      actor.state.tiene30Objeto,
      actor.state.tiene30Sujeto.get,
      actor.state.tiene30ObjetoVinculo)
    )
    result match {

      case d if d.equals(true) =>

        val newState = actor.state.copy(aplicarDescuento = Some(true))
        if(!actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("").equals("BAJA") && newState.aplicarDescuento.isDefined){
          actor.persistSnapshot(event, newState) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }
      case _ =>
        val newState = actor.state.copy(aplicarDescuento = Some(false))
        if(!actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("").equals("BAJA") && newState.aplicarDescuento.isDefined){
          actor.persistSnapshot(event, newState) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

