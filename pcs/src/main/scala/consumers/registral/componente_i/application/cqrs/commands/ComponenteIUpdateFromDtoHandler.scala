package consumers.registral.componente_i.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.componente_i.application.entities.ComponenteICommands.ComponenteIUpdateFromDto
import consumers.registral.componente_i.domain.ComponenteIEvents.ComponenteIUpdatedFromDto
import consumers.registral.componente_i.domain.ComponenteIState
import consumers.registral.componente_i.infrastructure.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class ComponenteIUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {
  def handle(command: ComponenteIUpdateFromDto)(state: ComponenteIState)(replyTo: ActorRef[Success]): ReplyEffect[ComponenteIUpdatedFromDto, ComponenteIState] = {
    Effect
      .persist[
        ComponenteIUpdatedFromDto,
        ComponenteIState
      ](
        ComponenteIUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.obligacionId,
          command.registro,
          command.detallesComponenteI
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
                ComponenteIUpdatedFromDto(
                  command.deliveryId,
                  command.sujetoId,
                  command.objetoId,
                  command.tipoObjeto,
                  command.obligacionId,
                  command.registro,
                  command.detallesComponenteI
                ).asJson.toString()
              )

          ),
          "ComponenteIPersistedSnapshot"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
