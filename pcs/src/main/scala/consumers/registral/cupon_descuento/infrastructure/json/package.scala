package consumers.registral.cupon_descuento.infrastructure

import ai.x.play.json.Jsonx
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoCommands.CuponDescuentoUpdateFromDto
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.{CuponDescuentoTri, DetallesCuponDescuento}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoResponses.GetCuponDescuentoResponse
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.{CuponDescuentoPersistedSnapshot, CuponDescuentoRemoved, CuponDescuentoUpdatedFromDto}
import consumers.registral.cupon_descuento.domain.CuponDescuentoState
import io.leonard.TraitFormat
import io.leonard.TraitFormat.traitFormat
import play.api.libs.json.Json
import serialization.EventSerializer

package object json {
  implicit val localdatetimeF = serialization.advanced.LocalDateTimeSerializer.dateFormat

  //private implicit val ExencionFF = consumers.no_registral.objeto.infrastructure.json.ExencionF

  implicit val DetallesCuponDescuentoF = Json.format[DetallesCuponDescuento]
  implicit val CuponDescuentoUpdateFromDtoF = Jsonx.formatCaseClass[CuponDescuentoUpdateFromDto]

  implicit val CuponDescuentoTriF = Jsonx.formatCaseClass[CuponDescuentoTri]

  implicit val CuponDescuentoDto: TraitFormat[CuponDescuentoExternalDto] =
    (traitFormat[CuponDescuentoExternalDto]
      << CuponDescuentoTriF)
  implicit val CuponDescuentoUpdatedFromDtoF = Jsonx.formatCaseClass[CuponDescuentoUpdatedFromDto]
  class CuponDescuentoUpdatedFromDtoFS extends EventSerializer[CuponDescuentoUpdatedFromDto]
  implicit val CuponDescuentoRemovedF = Jsonx.formatCaseClass[CuponDescuentoRemoved]
  class CuponDescuentoRemovedFS extends EventSerializer[CuponDescuentoRemoved]

  implicit val CuponDescuentoStateF = Jsonx.formatCaseClass[CuponDescuentoState]


  implicit val CuponDescuentoPersistedSnapshotF = Json.format[CuponDescuentoPersistedSnapshot]
  class CuponDescuentoPersistedSnapshotFS extends EventSerializer[CuponDescuentoPersistedSnapshot]
  implicit val GetCuponDescuentoResponseF = Json.format[GetCuponDescuentoResponse]
}
