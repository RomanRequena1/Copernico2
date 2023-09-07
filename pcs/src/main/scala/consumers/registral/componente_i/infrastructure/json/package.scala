package consumers.registral.componente_i.infrastructure

import ai.x.play.json.Jsonx
import consumers.registral.componente_i.application.entities.ComponenteICommands.ComponenteIUpdateFromDto
import consumers.registral.componente_i.application.entities.ComponenteIExternalDto
import consumers.registral.componente_i.application.entities.ComponenteIExternalDto.{ComponenteITri, DetallesComponenteI}
import consumers.registral.componente_i.application.entities.ComponenteIResponses.GetComponenteIResponse
import consumers.registral.componente_i.domain.ComponenteIEvents.{ComponenteIPersistedSnapshot, ComponenteIRemoved, ComponenteIUpdatedFromDto}
import consumers.registral.componente_i.domain.ComponenteIState
import io.leonard.TraitFormat
import io.leonard.TraitFormat.traitFormat
import play.api.libs.json.Json
import serialization.EventSerializer

package object json {
  implicit val localdatetimeF = serialization.advanced.LocalDateTimeSerializer.dateFormat

  //private implicit val ExencionFF = consumers.no_registral.objeto.infrastructure.json.ExencionF

  implicit val DetallesComponenteIF = Json.format[DetallesComponenteI]
  implicit val ComponenteIUpdateFromDtoF = Jsonx.formatCaseClass[ComponenteIUpdateFromDto]

  implicit val ComponenteITriF = Jsonx.formatCaseClass[ComponenteITri]

  implicit val ComponenteIDto: TraitFormat[ComponenteIExternalDto] =
    (traitFormat[ComponenteIExternalDto]
      << ComponenteITriF)
  implicit val ComponenteIUpdatedFromDtoF = Jsonx.formatCaseClass[ComponenteIUpdatedFromDto]
  class ComponenteIUpdatedFromDtoFS extends EventSerializer[ComponenteIUpdatedFromDto]
  implicit val ComponenteIRemovedF = Jsonx.formatCaseClass[ComponenteIRemoved]
  class ComponenteIRemovedFS extends EventSerializer[ComponenteIRemoved]

  implicit val ComponenteIStateF = Jsonx.formatCaseClass[ComponenteIState]


  implicit val ComponenteIPersistedSnapshotF = Json.format[ComponenteIPersistedSnapshot]
  class ComponenteIPersistedSnapshotFS extends EventSerializer[ComponenteIPersistedSnapshot]
  implicit val GetComponenteIResponseF = Json.format[GetComponenteIResponse]
}
