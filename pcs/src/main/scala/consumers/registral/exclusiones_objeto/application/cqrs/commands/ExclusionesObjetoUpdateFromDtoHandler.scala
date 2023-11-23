package consumers.registral.exclusiones_objeto.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto
import consumers.registral.exclusiones_objeto.domain.ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
import consumers.registral.exclusiones_objeto.domain.ExclusionesObjetoState
import consumers.registral.exclusiones_objeto.infrastructure.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer


class ExclusionesObjetoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {

  def handle(command: ExclusionesObjetoUpdateFromDto)(state: ExclusionesObjetoState)(replyTo: ActorRef[Success]) =

    Effect
      .persist[
        ExclusionesObjetoUpdatedFromDto,
        ExclusionesObjetoState
      ](
        ExclusionesObjetoUpdatedFromDto(
          command.deliveryId,
          command.objetoId,
          command.registro
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(command.aggregateRoot,
              ExclusionesObjetoUpdatedFromDto(
                command.deliveryId,
                command.objetoId,
                command.registro
              ).asJson.toString()
            )), "ExclusionesObjetoUpdatedFromDto"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
}