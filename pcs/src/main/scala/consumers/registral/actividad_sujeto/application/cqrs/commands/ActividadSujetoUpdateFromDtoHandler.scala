package consumers.registral.actividad_sujeto.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import consumers.registral.actividad_sujeto.domain.ActividadSujetoState
import consumers.registral.actividad_sujeto.infrastructure.json.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class ActividadSujetoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: ActividadSujetoUpdateFromDto)(state: ActividadSujetoState)(replyTo: ActorRef[Success]) = {
    Effect
      .persist[
        ActividadSujetoUpdatedFromDto,
        ActividadSujetoState
      ](
        ActividadSujetoUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.actividadSujetoId,
          command.registro
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
                ActividadSujetoUpdatedFromDto(
                  command.deliveryId,
                  command.sujetoId,
                  command.actividadSujetoId,
                  command.registro
                ).asJson.toString()
            )
          ),
          "ActividadSujetoUpdatedFromDto"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }

}
