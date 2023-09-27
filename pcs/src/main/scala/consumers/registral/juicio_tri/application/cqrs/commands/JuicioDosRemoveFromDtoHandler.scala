package consumers.registral.juicio_tri.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.juicio_tri.application.entities.JuicioDosCommands.JuicioDosRemoveFromDto
import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosRemovedFromDto
import consumers.registral.juicio_tri.domain.JuicioDosState
import consumers.registral.juicio_tri.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class JuicioDosRemoveFromDtoHandler(implicit messageProducer: MessageProducer){

  def handle(command: JuicioDosRemoveFromDto)(state: JuicioDosState)(replyTo: ActorRef[Success]): ReplyEffect[JuicioDosRemovedFromDto, JuicioDosState] = {

    if(isIdempotent(command, state.lastDeliveryIdByEvents)){
      println(s"[ ${command.aggregateRoot}] -juicio_tri- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + state.lastDeliveryIdByEvents)
      Effect.reply(replyTo)(Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)))
    }else {
      Effect
        .persist[
          JuicioDosRemovedFromDto,
          JuicioDosState
        ](
          JuicioDosRemovedFromDto(
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
                  JuicioDosRemovedFromDto(
                    command.juicioId,
                    command.deliveryId,
                    command.registro
                  )
                )
              )
            ),
            "JuicioDosRemovedSnapshot"
          )(_ => ())
        )
        .thenReply(replyTo) { state =>
          Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
        }
    }
  }
}
