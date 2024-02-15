package consumers.no_registral.exclusiones_objeto.infrastructure.json
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoResponses.GetExclusionesObjetoResponse
import consumers.no_registral.exclusiones_objeto.application.entities.{ExclusionesObjetoAnt, ExclusionesObjetoExternalDto, ExclusionesObjetoTri}
import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try
object ExclusionesObjetoImplicits {

  //todo COMMANDS
  implicit val ExclusionesObjetoUpdateFromDtoDecoder: Decoder[ExclusionesObjetoUpdateFromDto] = deriveDecoder
  implicit val ExclusionesObjetoUpdateFromDtoEncoder: Encoder[ExclusionesObjetoUpdateFromDto] = deriveEncoder

  //todo EXTERNAL DTO
  implicit val ExclusionesObjetoTriDecoder: Decoder[ExclusionesObjetoTri] = deriveDecoder
  implicit val ExclusionesObjetoTriEncoder: Encoder[ExclusionesObjetoTri] = deriveEncoder

  implicit val ExclusionesObjetoAntDecoder: Decoder[ExclusionesObjetoAnt] = deriveDecoder
  implicit val ExclusionesObjetoAntEncoder: Encoder[ExclusionesObjetoAnt] = deriveEncoder
  implicit val ExclusionesObjetoExternalDtoDecoder: Decoder[ExclusionesObjetoExternalDto] = deriveDecoder
  implicit val ExclusionesObjetoExternalDtoEncoder: Encoder[ExclusionesObjetoExternalDto] = deriveEncoder
  //todo RESPONSES
  implicit val GetExclusionesObjetoResponseDecoder: Decoder[GetExclusionesObjetoResponse] = deriveDecoder
  implicit val GetExclusionesObjetoResponseEncoder: Encoder[GetExclusionesObjetoResponse] = deriveEncoder

  ExclusionesObjetoUpdatedFromDto
  implicit val ExclusionesObjetoUpdatedFromDtoDecoder: Decoder[ExclusionesObjetoUpdatedFromDto] = deriveDecoder
  implicit val ExclusionesObjetoUpdatedFromDtoEncoder: Encoder[ExclusionesObjetoUpdatedFromDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }

}
