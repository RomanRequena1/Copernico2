package consumers.registral.juicio_obn.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.juicio_obn.infrastructure.json._
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.JuicioObnUpdateFromDto
import consumers.registral.juicio_obn.domain.JuicioObnEvents.JuicioObnUpdatedFromDto
import consumers.registral.juicio_obn.domain.JuicioObnState
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import org.slf4j.LoggerFactory

class JuicioObnUpdateFromDtoHandler(actor: JuicioObnActor)(implicit messageProducer: MessageProducer) {
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
    //println("CUMBIA -> COMMAN -> " + e.deliveryId + " id " + e)
    if(isIdempotent(command, state.lastDeliveryIdByEvent)){
      println(s"[ ${command.aggregateRoot}] -juicio_obn- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + state.lastDeliveryIdByEvent)

      // Informs that operation has been ignored */
      //todo check if this is desirable, why? signal the sender??

      // In this case the sender is "EL OBJETO"
      //println("CUMBIA path sender" + sender.path)

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
                serialization.encode(
                  JuicioObnUpdatedFromDto(
                    command.deliveryId,
                    command.juicioObnId,
                    command.objetoId,
                    command.tipoObjeto,
                    command.obligacionId,
                    command.registro
                  )
                )
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
