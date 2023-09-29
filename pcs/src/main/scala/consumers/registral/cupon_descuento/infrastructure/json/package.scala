package consumers.registral.cupon_descuento.infrastructure

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoCommands.{CuponDescuentoRemove, CuponDescuentoUpdateFromDto}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoResponses.GetCuponDescuentoResponse
import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoTri, DetallesCuponDescuento}
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.{CuponDescuentoPersistedSnapshot, CuponDescuentoRemoved, CuponDescuentoUpdatedFromDto}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object json {
  implicit val CuponDescuentoRemoveDecoder: Decoder[CuponDescuentoRemove] = deriveDecoder
  implicit val CuponDescuentoRemoveEncoder: Encoder[CuponDescuentoRemove] = deriveEncoder
  implicit val CuponDescuentoUpdateFromDtoDecoder: Decoder[CuponDescuentoUpdateFromDto] = deriveDecoder
  implicit val CuponDescuentoUpdateFromDtoEncoder: Encoder[CuponDescuentoUpdateFromDto] = deriveEncoder

  implicit val CuponDescuentoTriDecoder: Decoder[CuponDescuentoTri] = deriveDecoder
  implicit val CuponDescuentoTriEncoder: Encoder[CuponDescuentoTri] = deriveEncoder

  implicit val DetallesCuponDescuentoDecoder: Decoder[DetallesCuponDescuento] = deriveDecoder
  implicit val DetallesCuponDescuentoEncoder: Encoder[DetallesCuponDescuento] = deriveEncoder

  implicit val GetCuponDescuentoResponseDecoder: Decoder[GetCuponDescuentoResponse] = deriveDecoder
  implicit val GetCuponDescuentoResponseEncoder: Encoder[GetCuponDescuentoResponse] = deriveEncoder

  implicit val CuponDescuentoPersistedSnapshotDecoder: Decoder[CuponDescuentoPersistedSnapshot] = deriveDecoder
  implicit val CuponDescuentoPersistedSnapshotEncoder: Encoder[CuponDescuentoPersistedSnapshot] = deriveEncoder


  implicit val CuponDescuentoUpdatedFromDtoDecoder: Decoder[CuponDescuentoUpdatedFromDto] = deriveDecoder
  implicit val CuponDescuentoUpdatedFromDtoEncoder: Encoder[CuponDescuentoUpdatedFromDto] = deriveEncoder


  implicit val CuponDescuentoRemovedDecoder: Decoder[CuponDescuentoRemoved] = deriveDecoder
  implicit val CuponDescuentoRemovedEncoder: Encoder[CuponDescuentoRemoved] = deriveEncoder

}
