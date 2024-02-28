package consumers.registral.subasta.infrastructure.json

import consumers.registral.subasta.application.entities.SubastaCommands.SubastaUpdateFromDto
import consumers.registral.subasta.application.entities.SubastaExternalDto
import consumers.registral.subasta.application.entities.SubastaResponses.GetSubastaResponse
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try
object json {

  //DTO
  implicit val SubastaExternalDtoDecoder: Decoder[SubastaExternalDto] = deriveDecoder
  implicit val SubastaExternalDtoEncoder: Encoder[SubastaExternalDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
  //EVENT
  implicit val SubastaUpdatedFromDtoDecoder: Decoder[SubastaUpdatedFromDto] = deriveDecoder
  implicit val SubastaUpdatedFromDtoEncoder: Encoder[SubastaUpdatedFromDto] = deriveEncoder


  //COMMAND

  implicit val SubastaUpdateFromDtoDecoder: Decoder[SubastaUpdateFromDto] = deriveDecoder
  implicit val SubastaUpdateFromDtoEncoder: Encoder[SubastaUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val GetSubastaResponseDecoder: Decoder[GetSubastaResponse] = deriveDecoder
  implicit val GetSubastaResponseEncoder: Encoder[GetSubastaResponse] = deriveEncoder

}
