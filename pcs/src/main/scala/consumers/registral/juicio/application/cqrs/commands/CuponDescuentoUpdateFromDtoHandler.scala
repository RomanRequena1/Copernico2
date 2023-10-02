package consumers.registral.juicio.application.cqrs.commands

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoCommands.CuponDescuentoUpdateFromDto
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.CuponDescuentoUpdatedFromDto
import consumers.registral.cupon_descuento.domain.CuponDescuentoState
import consumers.registral.cupon_descuento.domain.events.CuponDescuentoUpdatedFromDtoHandler
import consumers.registral.cupon_descuento.infrastructure.json.json._
import design_principles.actor_model.Response
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class CuponDescuentoUpdateFromDtoHandler(implicit messageProducer: MessageProducer) {
  def handle(command: CuponDescuentoUpdateFromDto)(state: CuponDescuentoState)(replyTo: ActorRef[Success]): ReplyEffect[CuponDescuentoUpdatedFromDto, CuponDescuentoState] = {
    Effect
      .persist[
        CuponDescuentoUpdatedFromDto,
        CuponDescuentoState
      ](
        CuponDescuentoUpdatedFromDto(
          command.deliveryId,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.obligacionId,
          command.registro,
          command.detallesCuponDescuento
        )
      )
      .thenRun(state =>
        messageProducer.produce(
          Seq(
            KafkaKeyValue(
              command.aggregateRoot,
                CuponDescuentoUpdatedFromDto(
                  command.deliveryId,
                  command.sujetoId,
                  command.objetoId,
                  command.tipoObjeto,
                  command.obligacionId,
                  command.registro,
                  command.detallesCuponDescuento
                ).asJson.toString()
            )
          ),
          "CuponDescuentoPersistedSnapshot"
        )(_ => ())
      )
      .thenReply(replyTo) { state =>
        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      }
  }
}
