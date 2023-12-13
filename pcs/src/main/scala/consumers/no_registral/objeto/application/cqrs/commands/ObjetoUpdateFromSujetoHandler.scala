package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.helper.QueryExclusionObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromSujeto
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class ObjetoUpdateFromSujetoHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromSujeto] {
  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromSujeto
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = ObjetoUpdatedFromSujeto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto
    )
    actor.state += event
    val exclusionObjeto = QueryExclusionObjeto(command.objetoId)
    DMNTreintaPorcientoFinal.dmn(actor.state, command, exclusionObjeto)
      .fold(e => {
        log.error("ERROR DMN OBJETO: " + e)
      },
        {
          case d if d.value.equals(true) =>
            val newState = actor.state.copy(aplicarDescuento = Some(true))
            if(!actor.state.registro.get.SOJ_ESTADO.get.equals("BAJA")){
              actor.persistSnapshot(event, newState) { () =>
                sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
              }
            }
            //.state = newState
          //todo solo persistir en readside
          case _ =>
            val newState = actor.state.copy(aplicarDescuento = Some(false))
            if(!actor.state.registro.get.SOJ_ESTADO.get.equals("BAJA")){
              actor.persistSnapshot(event, newState) { () =>
                sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
              }
            }


        })
    //todo dmn
    //todo print campos que entran al dmn y la salida
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
