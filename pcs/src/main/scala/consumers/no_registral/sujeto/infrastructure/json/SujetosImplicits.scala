package consumers.no_registral.sujeto.infrastructure.json

import consumers.no_registral.sujeto.application.entity.SujetoCommands.{SujetoSetBajaFromObjeto, SujetoUpdateFromAnt, SujetoUpdateFromObjeto, SujetoUpdateFromTri}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.{SujetoAnt, SujetoTri}
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers.no_registral.sujeto.domain.SujetoEvents._
import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try


object SujetosImplicits {
  //COMMANDS


  implicit val SujetoUpdateFromTriDecoder: Decoder[SujetoUpdateFromTri] = deriveDecoder
  implicit val SujetoUpdateFromTriEncoder: Encoder[SujetoUpdateFromTri] = deriveEncoder

  implicit val SujetoUpdateFromAntDecoder: Decoder[SujetoUpdateFromAnt] = deriveDecoder
  implicit val SujetoUpdateFromAntEncoder: Encoder[SujetoUpdateFromAnt] = deriveEncoder



  implicit val SujetoUpdateFromObjetoDecoder: Decoder[SujetoUpdateFromObjeto] = deriveDecoder
  implicit val SujetoUpdateFromObjetoEncoder: Encoder[SujetoUpdateFromObjeto] = deriveEncoder

  implicit val SujetoSetBajaFromObjetoDecoder: Decoder[SujetoSetBajaFromObjeto] = deriveDecoder
  implicit val SujetoSetBajaFromObjetoEncoder: Encoder[SujetoSetBajaFromObjeto] = deriveEncoder

  //EXTERNALDTO


  implicit val SujetoAntDecoder: Decoder[SujetoAnt] = deriveDecoder
  implicit val SujetoAntEncoder: Encoder[SujetoAnt] = deriveEncoder

  implicit val SujetoTriDecoder: Decoder[SujetoTri] = deriveDecoder
  implicit val SujetoTriEncoder: Encoder[SujetoTri] = deriveEncoder

  implicit val GetSujetoResponseDecoder: Decoder[GetSujetoResponse] = deriveDecoder
  implicit val GetSujetoResponseEncoder: Encoder[GetSujetoResponse] = deriveEncoder

  implicit val SujetoExternalDtoDecoder: Decoder[SujetoExternalDto] = deriveDecoder
  implicit val SujetoExternalDtoEncoder: Encoder[SujetoExternalDto] = deriveEncoder

  //EVENTS

  implicit val SujetoSnapshotPersistedDecoder: Decoder[SujetoSnapshotPersisted] = deriveDecoder
  implicit val SujetoSnapshotPersistedEncoder: Encoder[SujetoSnapshotPersisted] = deriveEncoder


  implicit val SujetoUpdatedFromTriDecoder: Decoder[SujetoUpdatedFromTri] = deriveDecoder
  implicit val SujetoUpdatedFromTriEncoder: Encoder[SujetoUpdatedFromTri] = deriveEncoder

  implicit val SujetoUpdatedFromAntDecoder: Decoder[SujetoUpdatedFromAnt] = deriveDecoder
  implicit val SujetoUpdatedFromAntEncoder: Encoder[SujetoUpdatedFromAnt] = deriveEncoder


  implicit val SujetoUpdatedFromObjetoDecoder: Decoder[SujetoUpdatedFromObjeto] = deriveDecoder
  implicit val SujetoUpdatedFromObjetoEncoder: Encoder[SujetoUpdatedFromObjeto] = deriveEncoder

  implicit val SujetoBajaFromObjetoSetDecoder: Decoder[SujetoBajaFromObjetoSet] = deriveDecoder
  implicit val SujetoBajaFromObjetoSetEncoder: Encoder[SujetoBajaFromObjetoSet] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }

}
