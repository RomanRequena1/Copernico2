package consumers.registral.objeto_juicio.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.ObjetoJuicioUpdateFromDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState
import consumers.registral.objeto_juicio.infrastructure.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class ObjetoJuicioUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {
  def handle(
      command: ObjetoJuicioUpdateFromDto
  )(state: ObjetoJuicioState)(replyTo: ActorRef[Success]): ReplyEffect[ObjetoJuicioUpdatedFromDto, ObjetoJuicioState] = {
    Effect
      .persist[
        ObjetoJuicioUpdatedFromDto,
        ObjetoJuicioState
      ](
        ObjetoJuicioUpdatedFromDto(
          command.deliveryId,
          command.objetoId,
          command.tipoObjeto,
          command.juicioId,
          command.planId,
          command.registro
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
              ObjetoJuicioUpdatedFromDto(
                  command.deliveryId,
                  command.objetoId,
                  command.tipoObjeto,
                  command.juicioId,
                  command.planId,
                  command.registro
              ).asJson.toString()
            )
          ),
          "ObjetoJuicioUpdatedFromDto"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
