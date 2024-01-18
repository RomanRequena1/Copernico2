package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
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
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto
    )
    actor.state += event
   // val exclusionObjeto = QueryExclusionObjeto(command.objetoId)
    val obj_default = new ObjetosTri(Some("None"),0,"None","None","None",Some("None"),Some("None"),Some("None"),None,None,Some("None"),None,Some(0),Some("None"),Some(0),Some("None"),Some("None"),Some("None"),Some("None"))


    val result = DMNTreintaPorcientoFinal.calcularDmnFinal(DmnFinal("",
      "",
      actor.state.clasificacionObjeto,
      actor.state.tiene30Objeto,
      actor.state.tiene30Sujeto.get)
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
//    DMNTreintaPorcientoFinal.dmn(actor.state, command, "")
//      .fold(e => {
//        log.error("ERROR DMN OBJETO: " + e)
//      },
//        {
//          case d if d.value.equals(true) =>
//            val newState = actor.state.copy(aplicarDescuento = Some(true))
//            if(!actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("").equals("BAJA")){
//              actor.persistSnapshot(event, newState) { () =>
//                sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//              }
//            }
//            //.state = newState
//          //todo solo persistir en readside
//          case _ =>
//            val newState = actor.state.copy(aplicarDescuento = Some(false))
//            if(!actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("").equals("BAJA")){
//              actor.persistSnapshot(event, newState) { () =>
//                sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//              }
//            }
//        })
    //todo dmn
    //todo print campos que entran al dmn y la salida
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

