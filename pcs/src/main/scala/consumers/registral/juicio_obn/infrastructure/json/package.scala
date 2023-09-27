package consumers.registral.juicio_obn.infrastructure

import ai.x.play.json.Jsonx
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.{JuicioObnDeleteFromDto, JuicioObnUpdateFromDto}
import consumers.registral.juicio_obn.application.entities.{DetallesJuicioTri, JuicioObnTri}
import consumers.registral.juicio_obn.application.entities.JuicioObnResponses.GetJuicioObnResponses
import consumers.registral.juicio_obn.domain.JuicioObnEvents.{JuicioObnDeletedFromDto, JuicioObnUpdatedFromDto}
import play.api.libs.json.Json
import serialization.EventSerializer


package object json {
  implicit val localdatetimeF = serialization.advanced.LocalDateTimeSerializer.dateFormat
  implicit val detallesJuicioTriF = Json.format[DetallesJuicioTri]
  implicit val JuicioObnTriF = Jsonx.formatCaseClass[JuicioObnTri]

  //implicit val JuicioObnUpdateFromDtoF = Json.format[JuicioObnUpdateFromDto]
  implicit val JuicioObnUpdatedFromDtoF = Jsonx.formatCaseClass[JuicioObnUpdatedFromDto]
  class JuicioObnUpdatedFromDtoFS extends EventSerializer[JuicioObnUpdatedFromDto]

  implicit val JuicioObnDeledFromDtoF = Jsonx.formatCaseClass[JuicioObnDeletedFromDto]

  class JuicioObnDeletedFromDtoFS extends EventSerializer[JuicioObnDeletedFromDto]

  implicit val GetJuicioObnResponseF = Json.format[GetJuicioObnResponses]
}
