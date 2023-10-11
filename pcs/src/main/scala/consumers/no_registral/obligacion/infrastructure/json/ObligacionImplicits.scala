package consumers.no_registral.obligacion.infrastructure.json

import consumers.no_registral.objeto.application.entities.Exencion
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{ObligacionRemove, ObligacionUpdateExencion, ObligacionUpdateFromDto}
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{DetallesObligacion, ListDetallesObligaciones, ObligacionesAnt, ObligacionesTri}
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.obligacion.domain.ObligacionEvents.{ObligacionAddedExencion, ObligacionPersistedSnapshot, ObligacionRemoved, ObligacionUpdatedFromDto}
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

//  implicit val ObligacionesTriDecoder: Decoder[ObligacionExternalDto.ObligacionesTri] = deriveDecoder
//  implicit val ObligacionesTriEncoder: Encoder[ObligacionExternalDto.ObligacionesTri] = deriveEncoder
//
//  implicit val ObligacionesAntDecoder: Decoder[ObligacionExternalDto.ObligacionesAnt] = deriveDecoder
//  implicit val ObligacionesAntEncoder: Encoder[ObligacionExternalDto.ObligacionesAnt] = deriveEncoder

  implicit val DetallesObligacionDecoder: Decoder[DetallesObligacion] = deriveDecoder
  implicit val DetallesObligacionEncoder: Encoder[DetallesObligacion] = deriveEncoder

  implicit val ListDetallesObligacionesDecoder: Decoder[ListDetallesObligaciones] = deriveDecoder




  implicit val ListDetallesObligacionesEncoder: Encoder[ListDetallesObligaciones] =
    (detallesObligaciones: ListDetallesObligaciones) =>
      Json.obj(
        "BOB_DETALLES" -> detallesObligaciones.BOB_DETALLES.asJson
      )

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


  implicit val encodeTri: Encoder[ObligacionesTri] = deriveEncoder
  implicit val decodeTri: Decoder[ObligacionesTri] = deriveDecoder
  implicit val encodeAnt: Encoder[ObligacionesAnt] = deriveEncoder
  implicit val decodeAnt: Decoder[ObligacionesAnt] = deriveDecoder

  implicit val encodeObligacionExternalDto: Encoder[ObligacionExternalDto] = Encoder.instance {
    case x: ObligacionesTri => x.asJson
    case t: ObligacionesAnt => t.asJson
  }

  implicit val decodeObligacionExternalDto: Decoder[ObligacionExternalDto] = for {
    visitorType <- Decoder[String].prepare(_.downField("type"))
    value <- visitorType match {
      case "ObligacionesTri" => Decoder[ObligacionesTri]
      case "ObligacionesAnt" => Decoder[ObligacionesAnt]
      case other => Decoder.failedWithMessage(s"invalid type: $other")
    }
  } yield value



}
