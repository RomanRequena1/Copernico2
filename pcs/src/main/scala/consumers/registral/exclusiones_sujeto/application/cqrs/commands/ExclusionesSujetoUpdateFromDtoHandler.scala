package consumers.registral.exclusiones_sujeto.application.cqrs.commands

import akka.actor.typed.ActorRef
import akka.actor.Status.Success
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoCommands.ExclusionesSujetoUpdateFromDto
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoState
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import consumers.registral.exclusiones_sujeto.infrastructure.json._
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer


class ExclusionesSujetoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: ExclusionesSujetoUpdateFromDto)(state: ExclusionesSujetoState)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        ExclusionesSujetoUpdatedFromDto,
        ExclusionesSujetoState
      ](
        ExclusionesSujetoUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.registro
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(command.aggregateRoot,
              ExclusionesSujetoUpdatedFromDto(
                command.deliveryId,
                command.sujetoId,
                command.registro
              ).asJson.toString()
            )), "ExclusionesSujetoUpdatedFromDto"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
}