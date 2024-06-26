package consumers.registral.cupon_descuento.infrastructure.json

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoCommands.{CuponDescuentoRemove, CuponDescuentoUpdateFromDto}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoResponses.GetCuponDescuentoResponse
import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoTri, DetallesCuponDescuento, ListDetalleCuponDescuenta}
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.{CuponDescuentoPersistedSnapshot, CuponDescuentoRemoved, CuponDescuentoUpdatedFromDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object json {
  implicit val CuponDescuentoRemoveDecoder: Decoder[CuponDescuentoRemove] = deriveDecoder
  implicit val CuponDescuentoRemoveEncoder: Encoder[CuponDescuentoRemove] = deriveEncoder
  implicit val CuponDescuentoUpdateFromDtoDecoder: Decoder[CuponDescuentoUpdateFromDto] = deriveDecoder
  implicit val CuponDescuentoUpdateFromDtoEncoder: Encoder[CuponDescuentoUpdateFromDto] = deriveEncoder

  implicit val CuponDescuentoTriDecoder: Decoder[CuponDescuentoTri] = deriveDecoder
  implicit val CuponDescuentoTriEncoder: Encoder[CuponDescuentoTri] = deriveEncoder

  implicit val DetallesCuponDescuentoDecoder: Decoder[DetallesCuponDescuento] = deriveDecoder
  implicit val DetallesCuponDescuentoEncoder: Encoder[DetallesCuponDescuento] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }

  implicit val GetCuponDescuentoResponseDecoder: Decoder[GetCuponDescuentoResponse] = deriveDecoder
  implicit val GetCuponDescuentoResponseEncoder: Encoder[GetCuponDescuentoResponse] = deriveEncoder

  implicit val CuponDescuentoPersistedSnapshotDecoder: Decoder[CuponDescuentoPersistedSnapshot] = deriveDecoder
  implicit val CuponDescuentoPersistedSnapshotEncoder: Encoder[CuponDescuentoPersistedSnapshot] = deriveEncoder
  implicit val ListDetallesComponenteIDecoder: Decoder[ListDetalleCuponDescuenta] = deriveDecoder


  implicit val ListDetallesComponenteIEncoder: Encoder[ListDetalleCuponDescuenta] =
    (detallesObligaciones: ListDetalleCuponDescuenta) =>
      Json.obj(
        "BCD_DETALLES" -> detallesObligaciones.BCD_DETALLES.asJson
      )

  implicit val CuponDescuentoUpdatedFromDtoDecoder: Decoder[CuponDescuentoUpdatedFromDto] = deriveDecoder
  implicit val CuponDescuentoUpdatedFromDtoEncoder: Encoder[CuponDescuentoUpdatedFromDto] = deriveEncoder


  implicit val CuponDescuentoRemovedDecoder: Decoder[CuponDescuentoRemoved] = deriveDecoder
  implicit val CuponDescuentoRemovedEncoder: Encoder[CuponDescuentoRemoved] = deriveEncoder

}
