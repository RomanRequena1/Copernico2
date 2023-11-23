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
      command.deuda30Sujeto,
      command.exclusionSUjeto
    )
    actor.state += event
    println("TREINTA 12 " + actor.state)
    val exclusionObjeto = QueryExclusionObjeto(command.objetoId)
    DMNTreintaPorcientoFinal.dmn(actor.state, command, exclusionObjeto)
      .fold(e => {
        println("ERROR DMN OBJETO: " + e)
      },
        {
          case d if d.value.equals(true) =>
            println("TREINTA 12.0 " + d.value)
            val newState = actor.state.copy(aplicarDescuento = true)
            println("TREINTA 13.0 " + d.value)
            actor.persistSnapshot(event, newState) { () =>
              sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
            }
            //.state = newState
          //todo solo persistir en readside
          case _ =>
            println("TREINTA 12.1 ")
            val newState = actor.state.copy(aplicarDescuento = false)
            println("TREINTA 13.1 " + newState)
            actor.persistSnapshot(event, newState) { () =>
              sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
            }
        })
    //todo dmn
    //todo print campos que entran al dmn y la salida
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
