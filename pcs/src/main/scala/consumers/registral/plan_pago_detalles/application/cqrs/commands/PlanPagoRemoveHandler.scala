package consumers.registral.plan_pago_detalles.application.cqrs.commands

import io.circe.syntax.EncoderOps
import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoCommands.PlanPagoRemoveFromDto
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.PlanPagoRemovedFromDto
import consumers.registral.plan_pago_detalles.domain.PlanPagoState
import design_principles.actor_model.Response
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.plan_pago_detalles.infrastructure.json.json._

class PlanPagoRemoveHandler (implicit messageProducer: MessageProducer){

  def handle(command: PlanPagoRemoveFromDto)(state: PlanPagoState)(replyTo: ActorRef[Success]) = {
    val event = PlanPagoRemovedFromDto(
      command.deliveryId,
      command.planPagoId,
      command.tipoObjeto,
      command.objetoId,
      command.obligacionId,
      command.registro
    )
    Effect
      .persist[
        PlanPagoRemovedFromDto,
        PlanPagoState
      ](event)
      .thenRun(state =>
        messageProducer.produce
        (Seq(KafkaKeyValue(command.aggregateRoot,
          event.asJson.toString())),
          "PlanPagoObnRemovedFromDto")(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
