package consumers.registral.juicio_obn.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.JuicioObnUpdateFromDto
import consumers.registral.juicio_obn.domain.JuicioObnEvents.JuicioObnUpdatedFromDto
import consumers.registral.juicio_obn.domain.JuicioObnState
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import consumers.registral.juicio_obn.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import org.slf4j.LoggerFactory

class JuicioObnUpdateFromDtoHandler(actor: JuicioObnActor)(implicit messageProducer: MessageProducer) {
  private val log = LoggerFactory.getLogger(this.getClass)
  def handle(
            command: JuicioObnUpdateFromDto
            )(state: JuicioObnState)(replyTo: ActorRef[Success]): ReplyEffect[JuicioObnUpdatedFromDto, JuicioObnState] = {
    //val log = LoggerFactory.getLogger(this.getClass)
    val e = JuicioObnUpdatedFromDto(
      command.deliveryId,
      command.juicioObnId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.registro
    )

    if(isIdempotent(command, state.lastDeliveryIdByEvent)){
      log.debug(s"[ ${command.aggregateRoot}] -juicio_obn- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + state.lastDeliveryIdByEvent)

      Effect.reply(replyTo)(Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)))
    }
    else{
      Effect
        .persist[
          JuicioObnUpdatedFromDto,
          JuicioObnState
        ](
          JuicioObnUpdatedFromDto(
            command.deliveryId,
            command.juicioObnId,
            command.objetoId,
            command.tipoObjeto,
            command.obligacionId,
            command.registro
          )
        )
        .thenRun(_ =>
          messageProducer.produce(
            Seq(
              KafkaKeyValue(
                command.aggregateRoot,

                  JuicioObnUpdatedFromDto(
                    command.deliveryId,
                    command.juicioObnId,
                    command.objetoId,
                    command.tipoObjeto,
                    command.obligacionId,
                    command.registro
                  ).asJson.toString()
                              )
            ),
            "JuicioObnUpdatedFronDto"
          )(_ => ())
        )
        .thenReply(replyTo) { _ =>
          Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
        }
    }


  }

}
