package consumers.registral.juicio_tri.infrastructure

import consumers.registral.juicio_tri.application.entities.JuicioDosCommands.JuicioDosUpdateFromDto
import consumers.registral.juicio_tri.application.entities.JuicioDosExternalDto
import consumers.registral.juicio_tri.application.entities.JuicioDosExternalDto.JuicioDosTri
import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import consumers.registral.juicio_tri.domain.JuicioDosEvents.{JuicioDosRemovedFromDto, JuicioDosUpdatedFromDto}
import io.leonard.TraitFormat
import io.leonard.TraitFormat.traitFormat
import play.api.libs.json.Json
import serialization.EventSerializer

package object json {
  implicit val localdatetimeF = serialization.advanced.LocalDateTimeSerializer.dateFormat

  implicit val JuicioDosTriF = Json.format[JuicioDosTri]
  implicit val JuicioDosExternalDtoF: TraitFormat[JuicioDosExternalDto] =
    (traitFormat[JuicioDosExternalDto]
      << JuicioDosTriF)
  implicit val JuicioDosUpdateFromDtoF = Json.format[JuicioDosUpdateFromDto]

  implicit val JuicioDosUpdatedFromDtoF = Json.format[JuicioDosUpdatedFromDto]
  class JuicioDosUpdatedFromDtoFS extends EventSerializer[JuicioDosUpdatedFromDto]

  implicit val JuicioDosRemoveFromDtoF = Json.format[JuicioDosRemovedFromDto]
  class JuicioDosRemovedFromDtoFS extends EventSerializer[JuicioDosRemovedFromDto]

  implicit val GetJuicioDosResponseF = Json.format[GetJuicioDosResponse]
}
