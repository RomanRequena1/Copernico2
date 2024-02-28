package consumers.registral.juicio_tri.infrastructure.json

import consumers.registral.juicio_tri.application.entities.JuicioDosCommands.{JuicioDosRemoveFromDto, JuicioDosUpdateFromDto}
import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import consumers.registral.juicio_tri.application.entities.JuicioDosTri
import consumers.registral.juicio_tri.domain.JuicioDosEvents.{JuicioDosRemovedFromDto, JuicioDosUpdatedFromDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try


object json {

  //DTO
  implicit val JuicioDosTriDecoder: Decoder[JuicioDosTri] = deriveDecoder
  implicit val JuicioDosTriEncoder: Encoder[JuicioDosTri] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
  //EVENT
  implicit val JuicioDosUpdatedFromDtoDecoder: Decoder[JuicioDosUpdatedFromDto] = deriveDecoder
  implicit val JuicioDosUpdatedFromDtoEncoder: Encoder[JuicioDosUpdatedFromDto] = deriveEncoder

  implicit val JuicioDosRemovedFromDtoDecoder: Decoder[JuicioDosRemovedFromDto] = deriveDecoder
  implicit val JuicioDosRemovedFromDtoEncoder: Encoder[JuicioDosRemovedFromDto] = deriveEncoder

  //COMMAND

  implicit val JuicioDosUpdateFromDtoDecoder: Decoder[JuicioDosUpdateFromDto] = deriveDecoder
  implicit val JuicioDosUpdateFromDtoEncoder: Encoder[JuicioDosUpdateFromDto] = deriveEncoder

  implicit val JuicioDosRemoveFromDtoDecoder: Decoder[JuicioDosRemoveFromDto] = deriveDecoder
  implicit val JuicioDosRemoveFromDtoEncoder: Encoder[JuicioDosRemoveFromDto] = deriveEncoder

  //REPONDS

  implicit val GetJuicioDosResponseDecoder: Decoder[GetJuicioDosResponse] = deriveDecoder
  implicit val GetJuicioDosResponseEncoder: Encoder[GetJuicioDosResponse] = deriveEncoder

}
