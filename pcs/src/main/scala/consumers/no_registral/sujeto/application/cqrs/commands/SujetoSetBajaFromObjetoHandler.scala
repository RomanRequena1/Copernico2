package consumers.no_registral.sujeto.application.cqrs.commands

import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoSetBajaFromObjeto
import consumers.no_registral.sujeto.application.helper.SendToObjeto
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import scala.util.{Success, Try}

class SujetoSetBajaFromObjetoHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoSetBajaFromObjeto] {
  override def handle(command: SujetoSetBajaFromObjeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val event = SujetoEvents.SujetoBajaFromObjetoSet(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto
    )

    actor.persistEvent(event, Set("Sujeto")) { () =>

      actor.state += event
      SendToObjeto(actor.state, sender, actor.context, event.sujetoId, command.objetoId, command.tipoObjeto)
      actor.persistSnapshot()(_ => ())
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
