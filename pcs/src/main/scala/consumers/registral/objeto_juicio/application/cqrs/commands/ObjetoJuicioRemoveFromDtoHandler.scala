package consumers.registral.objeto_juicio.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.RemoveObjetoJuicioFromDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.{ObjetoJuicioRemovedFromDto, ObjetoJuicioUpdatedFromDto}
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState
import consumers.registral.objeto_juicio.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import org.slf4j.LoggerFactory

class ObjetoJuicioRemoveFromDtoHandler(implicit messageProducer: MessageProducer) {
  def handle(
              command: RemoveObjetoJuicioFromDto
            )(state: ObjetoJuicioState)(replyTo: ActorRef[Success]): ReplyEffect[ObjetoJuicioRemovedFromDto, ObjetoJuicioState] = {
    val log = LoggerFactory.getLogger(this.getClass)
    if (isIdempotent(command, state.lastDeliveryIdByEvent)) {
      log.error(s"[${command.aggregateRoot}] -objeto-juicio_obn- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + state.lastDeliveryIdByEvent)

      Effect.reply(replyTo)(Success(Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)))
    } else {
    Effect
      .persist[
        ObjetoJuicioRemovedFromDto,
        ObjetoJuicioState
      ](
        ObjetoJuicioRemovedFromDto(
          command.objetoId,
          command.tipoObjeto,
          command.idRel,
          command.tipoObjetoRel,
          command.tipoRel,
          command.idExterno,
          command.idExterno2,
          command.estado,
          command.deliveryId,
          command.registro
        )
      )
      .thenRun(_ =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
              ObjetoJuicioRemovedFromDto(
                command.objetoId,
                command.tipoObjeto,
                command.idRel,
                command.tipoObjetoRel,
                command.tipoRel,
                command.idExterno,
                command.idExterno2,
                command.estado,
                command.deliveryId,
                command.registro
              ).asJson.toString()
            )
          ),
          "ObjetoJuicioRemovedFromDto"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
    }
  }
}
