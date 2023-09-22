package consumers.registral.juicio_tri.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.juicio_tri.application.entities.JuicioDosCommands.JuicioDosUpdateFromDto
import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosUpdatedFromDto
import consumers.registral.juicio_tri.domain.JuicioDosState
import consumers.registral.juicio_tri.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class JuicioDosUpdateFromDtoHandler(implicit messageProducer: MessageProducer){
  def handle(command: JuicioDosUpdateFromDto)(state: JuicioDosState)(replyTo: ActorRef[Success]): ReplyEffect[JuicioDosUpdatedFromDto, JuicioDosState] = {

    if (isIdempotent(command, state.lastDeliveryIdByEvents)) {
      Effect.reply(replyTo)(Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)))

    } else {
      Effect
        .persist[
          JuicioDosUpdatedFromDto,
          JuicioDosState
        ](
          JuicioDosUpdatedFromDto(
            command.juicioId,
            command.deliveryId,
            command.registro
          )
        )
        .thenRun(state =>
          messageProducer.produce(
            Seq(
              KafkaKeyValue(
                command.aggregateRoot,
                serialization.encode(
                  JuicioDosUpdatedFromDto(
                    command.juicioId,
                    command.deliveryId,
                    command.registro
                  )
                )
              )
            ),
            "JuicioDosPersistedSnapshot"
          )(_ => ())
        )
        .thenReply(replyTo) { state =>
          Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
        }
    }
  }
}
