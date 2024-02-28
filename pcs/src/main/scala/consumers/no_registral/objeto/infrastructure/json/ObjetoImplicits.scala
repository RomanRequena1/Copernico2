package consumers.no_registral.objeto.infrastructure.json

import consumers.no_registral.objeto.application.entities.ObjetoCommands._
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto._
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetExencionResponse, GetObjetoResponse}
import consumers.no_registral.objeto.application.entities._
import consumers.no_registral.objeto.domain.ObjetoEvents._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object ObjetoImplicits {

  // COMMANDS
  implicit val ObjetoUpdateCotitularesDecoder: Decoder[ObjetoUpdateCotitulares] = deriveDecoder
  implicit val ObjetoUpdateCotitularesEncoder: Encoder[ObjetoUpdateCotitulares] = deriveEncoder
  implicit val ObjetoSnapshotDecoder: Decoder[ObjetoSnapshot] = deriveDecoder
  implicit val ObjetoSnapshotEncoder: Encoder[ObjetoSnapshot] = deriveEncoder

  implicit val ObjetoUpdateFromTriDecoder: Decoder[ObjetoUpdateFromTri] = deriveDecoder
  implicit val ObjetoUpdateFromTriEncoder: Encoder[ObjetoUpdateFromTri] = deriveEncoder

  implicit val ObjetoUpdateFromAntDecoder: Decoder[ObjetoUpdateFromAnt] = deriveDecoder
  implicit val ObjetoUpdateFromAntEncoder: Encoder[ObjetoUpdateFromAnt] = deriveEncoder

  implicit val ObjetoUpdateFromObligacionDecoder: Decoder[ObjetoUpdateFromObligacion] = deriveDecoder
  implicit val ObjetoUpdateFromObligacionEncoder: Encoder[ObjetoUpdateFromObligacion] = deriveEncoder

  implicit val ObjetoRemoveObligacionDecoder: Decoder[ObjetoRemoveObligacion] = deriveDecoder
  implicit val ObjetoRemoveObligacionEncoder: Encoder[ObjetoRemoveObligacion] = deriveEncoder

  implicit val ObjetoTagAddDecoder: Decoder[ObjetoTagAdd] = deriveDecoder
  implicit val ObjetoTagAddEncoder: Encoder[ObjetoTagAdd] = deriveEncoder

  implicit val SelfUpdateCotitularesDecoder: Decoder[SelfUpdateCotitulares] = deriveDecoder
  implicit val SelfUpdateCotitularesEncoder: Encoder[SelfUpdateCotitulares] = deriveEncoder

  implicit val ObjetoAddExencionDecoder: Decoder[ObjetoAddExencion] = deriveDecoder
  implicit val ObjetoAddExencionEncoder: Encoder[ObjetoAddExencion] = deriveEncoder

  implicit val SetBajaObjetoDecoder: Decoder[SetBajaObjeto] = deriveDecoder
  implicit val SetBajaObjetoEncoder: Encoder[SetBajaObjeto] = deriveEncoder

  implicit val ObjetoUpdateFromObnTreintaPorcientoDecoder: Decoder[ObjetoUpdateFromObnTreintaPorciento] = deriveDecoder
  implicit val ObjetoUpdateFromObnTreintaPorcientoEncoder: Encoder[ObjetoUpdateFromObnTreintaPorciento] = deriveEncoder

  // EXTERNALDTO

  implicit val ObjetosTriDecoder: Decoder[ObjetosTri] = deriveDecoder
  implicit val ObjetosTriEncoder: Encoder[ObjetosTri] = deriveEncoder

  implicit val ObjetosAntDecoder: Decoder[ObjetosAnt] = deriveDecoder
  implicit val ObjetosAntEncoder: Encoder[ObjetosAnt] = deriveEncoder

  implicit val DetallesObjetoDecoder: Decoder[DetallesObjeto] = deriveDecoder
  implicit val DetallesObjetoEncoder: Encoder[DetallesObjeto] = deriveEncoder

  implicit val ObjetosExternalDtoDecoder: Decoder[ObjetoExternalDto] = deriveDecoder
  implicit val ObjetosExternalDtoEncoder: Encoder[ObjetoExternalDto] = deriveEncoder

  implicit val ListDetallesObjetoDecoder: Decoder[ListDetallesObjeto] = deriveDecoder
  implicit val ListDetallesObjetoEncoder: Encoder[ListDetallesObjeto] =
    (detallesObjeto: ListDetallesObjeto) =>
      Json.obj(
        "SOJ_DETALLES" -> detallesObjeto.SOJ_DETALLES.asJson
      )

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }

  implicit val CotitularidadDecoder: Decoder[Cotitularidad] = deriveDecoder
  implicit val CotitularidadEncoder: Encoder[Cotitularidad] = deriveEncoder

  implicit val ExencionDecoder: Decoder[Exencion] = deriveDecoder
  implicit val ExencionEncoder: Encoder[Exencion] = deriveEncoder

  //RESPONSES
  implicit val GetObjetoResponseDecoder: Decoder[GetObjetoResponse] = deriveDecoder
  implicit val GetObjetoResponseEncoder: Encoder[GetObjetoResponse] = deriveEncoder

  implicit val GetExencionResponseDecoder: Decoder[GetExencionResponse] = deriveDecoder
  implicit val GetExencionResponseEncoder: Encoder[GetExencionResponse] = deriveEncoder

  // EVENTS
  implicit val ObjetoUpdatedCotitularesDecoder: Decoder[ObjetoUpdatedCotitulares] = deriveDecoder
  implicit val ObjetoUpdatedCotitularesEncoder: Encoder[ObjetoUpdatedCotitulares] = deriveEncoder

  implicit val ObjetoSnapshotPersistedDecoder: Decoder[ObjetoSnapshotPersisted] = deriveDecoder
  implicit val ObjetoSnapshotPersistedEncoder: Encoder[ObjetoSnapshotPersisted] = deriveEncoder

  implicit val ObjetoUpdatedFromTriDecoder: Decoder[ObjetoUpdatedFromTri] = deriveDecoder
  implicit val ObjetoUpdatedFromTriEncoder: Encoder[ObjetoUpdatedFromTri] = deriveEncoder

  implicit val ObjetoUpdatedFromAntDecoder: Decoder[ObjetoUpdatedFromAnt] = deriveDecoder
  implicit val ObjetoUpdatedFromAntEncoder: Encoder[ObjetoUpdatedFromAnt] = deriveEncoder

  implicit val ObjetoTagAddedDecoder: Decoder[ObjetoTagAdded] = deriveDecoder
  implicit val ObjetoTagAddedEncoder: Encoder[ObjetoTagAdded] = deriveEncoder

  implicit val ObjetoTagRemovedDecoder: Decoder[ObjetoTagRemoved] = deriveDecoder
  implicit val ObjetoTagRemovedEncoder: Encoder[ObjetoTagRemoved] = deriveEncoder

  implicit val ObjetoUpdatedFromObligacionDecoder: Decoder[ObjetoUpdatedFromObligacion] = deriveDecoder
  implicit val ObjetoUpdatedFromObligacionEncoder: Encoder[ObjetoUpdatedFromObligacion] = deriveEncoder

  implicit val ObjetoUpdatedFromObligacionBajaSetDecoder: Decoder[ObjetoUpdatedFromObligacionBajaSet] = deriveDecoder
  implicit val ObjetoUpdatedFromObligacionBajaSetEncoder: Encoder[ObjetoUpdatedFromObligacionBajaSet] = deriveEncoder

  implicit val ObjetoAddedExencionDecoder: Decoder[ObjetoAddedExencion] = deriveDecoder
  implicit val ObjetoAddedExencionEncoder: Encoder[ObjetoAddedExencion] = deriveEncoder

  implicit val ObjetoBajaSetDecoder: Decoder[ObjetoBajaSet] = deriveDecoder
  implicit val ObjetoBajaSetEncoder: Encoder[ObjetoBajaSet] = deriveEncoder

  implicit val ObjetoRemovedObligacionDecoder: Decoder[ObjetoRemovedObligacion] = deriveDecoder
  implicit val ObjetoRemovedObligacionEncoder: Encoder[ObjetoRemovedObligacion] = deriveEncoder

  implicit val ObjetoUpdatedFromObnTreintaProcientoDecoder: Decoder[ObjetoUpdatedFromObnTreintaProciento] = deriveDecoder
  implicit val ObjetoUpdatedFromObnTreintaProcientoEncoder: Encoder[ObjetoUpdatedFromObnTreintaProciento] = deriveEncoder

}