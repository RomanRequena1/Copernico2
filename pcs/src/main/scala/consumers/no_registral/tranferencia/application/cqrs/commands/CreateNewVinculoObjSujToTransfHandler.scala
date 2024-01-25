package consumers.no_registral.tranferencia.application.cqrs.commands

import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoSetBajaFromObjeto
import consumers.no_registral.tranferencia.application.entity.TransferenciaCommands.{CreateNewVinculoObjSujToTransf, CreateVinculoObjSujToTransf}
import consumers.no_registral.tranferencia.domain.TransferenciaEvent
import consumers.no_registral.tranferencia.domain.TransferenciaEvent.CreatedNewVinculoObjSujToTransf
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.TranferenciaActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class CreateNewVinculoObjSujToTransfHandler(actor: TranferenciaActor) extends SyncCommandHandler[CreateNewVinculoObjSujToTransf] {
  override def handle(command: CreateNewVinculoObjSujToTransf): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = TransferenciaEvent.CreatedNewVinculoObjSujToTransf(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30ObjetoTranf
    )

    actor.persistEvent(event) { () =>

      actor.state += event
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
