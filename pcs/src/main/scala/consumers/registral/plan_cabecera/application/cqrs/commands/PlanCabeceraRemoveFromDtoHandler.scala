package consumers.registral.plan_cabecera.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraCommands.PlanCabeceraRemoveFromDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.PlanCabeceraRemovedFromDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraState
import consumers.registral.plan_cabecera.infrastructure.json.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class PlanCabeceraRemoveFromDtoHandler (implicit messageProducer: MessageProducer) {
  def handle(command: PlanCabeceraRemoveFromDto)(state: PlanCabeceraState)(replyTo: ActorRef[Success]) = {
    val event = PlanCabeceraRemovedFromDto(
      command.deliveryId,
      command.planCabeceraId,
      command.registro
    )
    Effect
      .persist[
        PlanCabeceraRemovedFromDto,
        PlanCabeceraState
      ](event)
      .thenRun(state =>
        messageProducer.produce
        (Seq(KafkaKeyValue(command.aggregateRoot,
          event.asJson.toString())),
          "PlanCabeceraRemovedFromDto")(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
