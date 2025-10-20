package consumers.no_registral.obligacion.infrastructure.json

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{
  ObligacionRemove,
  ObligacionUpdateExencion,
  ObligacionUpdateFromDto
}
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.obligacion.application.entities._
import consumers.no_registral.obligacion.domain.ObligacionEvents.{
  ObligacionAddedExencion,
  ObligacionPersistedSnapshot,
  ObligacionRemoved,
  ObligacionUpdatedFromDto
}
import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object ObligacionImplicits {

  //COMMANDS
  implicit val ObligacionRemoveDecoder: Decoder[ObligacionRemove] = deriveDecoder
  implicit val ObligacionRemoveEncoder: Encoder[ObligacionRemove] = deriveEncoder
  implicit val ObligacionUpdateFromDtoDecoder: Decoder[ObligacionUpdateFromDto] = deriveDecoder
  implicit val ObligacionUpdateFromDtoEncoder: Encoder[ObligacionUpdateFromDto] = deriveEncoder
  implicit val ObligacionUpdateExencionDecoder: Decoder[ObligacionUpdateExencion] = deriveDecoder
  implicit val ObligacionUpdateExencionEncoder: Encoder[ObligacionUpdateExencion] = deriveEncoder
  implicit val ExencionDecoder: Decoder[Exencion] = deriveDecoder
  implicit val ExencionEncoder: Encoder[Exencion] = deriveEncoder

  //EXTERNALDTO

  implicit val ObligacionesTriDecoder: Decoder[ObligacionesTri] = deriveDecoder
  implicit val ObligacionesTriEncoder: Encoder[ObligacionesTri] = deriveEncoder

  implicit val ObligacionesAntDecoder: Decoder[ObligacionesAnt] = deriveDecoder
  implicit val ObligacionesAntEncoder: Encoder[ObligacionesAnt] = deriveEncoder

  implicit val DetallesObligacionDecoder: Decoder[DetallesObligacion] = deriveDecoder
  implicit val DetallesObligacionEncoder: Encoder[DetallesObligacion] = deriveEncoder

  implicit val DetallesCaracteristicasDecoder: Decoder[DetallesObligacionCaracteristicas] = deriveDecoder
  implicit val DetallesCaracteristicasEncoder: Encoder[DetallesObligacionCaracteristicas] = deriveEncoder

  implicit val DetallesSupresionesDecoder: Decoder[DetallesSupresiones] = deriveDecoder
  implicit val DetallesSupresionesEncoder: Encoder[DetallesSupresiones] = deriveEncoder

  implicit val ObligacionExternalDtoDecoder: Decoder[ObligacionExternalDto] = deriveDecoder
  implicit val ObligacionExternalDtoEncoder: Encoder[ObligacionExternalDto] = deriveEncoder

  implicit val ListDetallesObligacionesDecoder: Decoder[ListDetallesObligaciones] = deriveDecoder
  implicit val ListDetallesObligacionesEncoder: Encoder[ListDetallesObligaciones] =
    (detallesObligaciones: ListDetallesObligaciones) =>
      Json.obj(
        "BOB_DETALLES" -> detallesObligaciones.BOB_DETALLES.asJson
      )

  implicit val ListDetallesCaracteristicasDecoder: Decoder[ListCaracteristicasObligaciones] = deriveDecoder
  implicit val ListDetallesCaracteristicasEncoder: Encoder[ListCaracteristicasObligaciones] =
    (detallesCaracteristicas: ListCaracteristicasObligaciones) =>
      Json.obj(
        "BOB_DETALLES_CARACTERISTICAS" -> detallesCaracteristicas.BOB_DETALLES_CARACTERISTICAS.asJson
      )

  implicit val ListDetallesSupresionesDecoder: Decoder[ListDetallesSupresiones] = deriveDecoder
  implicit val ListDetallesSupresionesEncoder: Encoder[ListDetallesSupresiones] =
    (detallesSupresiones: ListDetallesSupresiones) =>
      Json.obj(
        "BOB_DETALLES_SUPRESIONES" -> detallesSupresiones.BOB_DETALLES_SUPRESIONES.asJson
      )

  // FIXME: Check si se puede validar o no fecha desde el decode para consumir el evento igual pero colocarle fecha default
  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))

  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }

  // RESPONSES
  implicit val GetObligacionResponseDecoder: Decoder[GetObligacionResponse] = deriveDecoder
  implicit val GetObligacionResponseEncoder: Encoder[GetObligacionResponse] = deriveEncoder

  //EVENTS
  implicit val ObligacionPersistedSnapshotDecoder: Decoder[ObligacionPersistedSnapshot] = deriveDecoder
  implicit val ObligacionPersistedSnapshotEncoder: Encoder[ObligacionPersistedSnapshot] = deriveEncoder

  implicit val ObligacionUpdatedFromDtoDecoder: Decoder[ObligacionUpdatedFromDto] = deriveDecoder
  implicit val ObligacionUpdatedFromDtoEncoder: Encoder[ObligacionUpdatedFromDto] = deriveEncoder

  implicit val ObligacionRemovedDecoder: Decoder[ObligacionRemoved] = deriveDecoder
  implicit val ObligacionRemovedEncoder: Encoder[ObligacionRemoved] = deriveEncoder

  implicit val ObligacionAddedExencionDecoder: Decoder[ObligacionAddedExencion] = deriveDecoder
  implicit val ObligacionAddedExencionEncoder: Encoder[ObligacionAddedExencion] = deriveEncoder

}
