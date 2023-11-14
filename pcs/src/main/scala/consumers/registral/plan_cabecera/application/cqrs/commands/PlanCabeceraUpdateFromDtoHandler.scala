package consumers.registral.plan_cabecera.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.actor.Status.Success
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraCommands.PlanCabeceraUpdateFromDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.PlanCabeceraUpdatedFromDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraState
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.plan_cabecera.infrastructure.json.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps


class PlanCabeceraUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: PlanCabeceraUpdateFromDto)(state: PlanCabeceraState)(replyTo: ActorRef[Success]) = {
    val event = PlanCabeceraUpdatedFromDto(
      command.deliveryId,
      command.planCabeceraId,
      command.registro
    )
    Effect
      .persist[
        PlanCabeceraUpdatedFromDto,
        PlanCabeceraState
      ](event)
      .thenRun(state =>
        messageProducer.produce
        (Seq(KafkaKeyValue(command.aggregateRoot,
          event.asJson.toString())),
          "PlanCabeceraUpdatedFromDto")(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
