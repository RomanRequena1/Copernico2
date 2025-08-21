package consumers.registral.objeto_juicio.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.ObjetoJuicioUpdateFromDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState
import consumers.registral.objeto_juicio.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import org.slf4j.LoggerFactory

class ObjetoJuicioUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {
  private val log = LoggerFactory.getLogger(this.getClass)

  def handle(
            command: ObjetoJuicioUpdateFromDto
            )(state: ObjetoJuicioState)(replyTo: ActorRef[Success]): ReplyEffect[ObjetoJuicioUpdatedFromDto, ObjetoJuicioState] = {

    if(isIdempotent(command, state.lastDeliveryIdByEvent)){
      log.warn(s"[ ${command.aggregateRoot}] -objeto-juicio_obn- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + state.lastDeliveryIdByEvent)
      Effect.reply(replyTo)(Success(Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)))
    }
    else {
      Effect
        .persist[
          ObjetoJuicioUpdatedFromDto,
          ObjetoJuicioState
        ](
          ObjetoJuicioUpdatedFromDto(
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
        .thenRun(state =>
          messageProducer.produce(
            Seq(
              KafkaKeyValue(
                command.aggregateRoot,
                ObjetoJuicioUpdatedFromDto(
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
            "ObjetoJuicioUpdatedFromDto"
          )(_ => ())
        )
        .thenReply(replyTo) { _ =>
          Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
        }
    }
  }
}
