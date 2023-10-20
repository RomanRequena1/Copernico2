package consumers.registral.domicilio_sujeto.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoState
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import consumers.registral.domicilio_sujeto.infrastructure.json._

class DomicilioSujetoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: DomicilioSujetoUpdateFromDto)(state: DomicilioSujetoState)(replyTo: ActorRef[Success]) =
    Effect
      .persist[
        DomicilioSujetoUpdatedFromDto,
        DomicilioSujetoState
      ](
        DomicilioSujetoUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.domicilioId,
          command.registro
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(command.aggregateRoot,
                DomicilioSujetoUpdatedFromDto(
                  command.deliveryId,
                  command.sujetoId,
                  command.domicilioId,
                  command.registro
                ).asJson.toString()
              )), "DomicilioSujetoUpdatedFromDto"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }

}
