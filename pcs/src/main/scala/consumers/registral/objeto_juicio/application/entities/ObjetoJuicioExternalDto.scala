package consumers.registral.objeto_juicio.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.{ObjetoJuicioAnt, ObjetoJuicioTri}
import serialization.CbroSerialization

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[ObjetoJuicioTri], name = "ObjetoJuicioTri"),
    new JsonSubTypes.Type(value = classOf[ObjetoJuicioAnt], name = "ObjetoJuicioAnt"),
  )
)
sealed trait ObjetoJuicioExternalDto extends ddd.ExternalDto {
  def EV_ID: String
  def OJU_SOJ_IDENTIFICADOR: String
  def OJU_SOJ_TIPO_OBJETO: String
  def OJU_JUI_ID: String
  def OJU_PLAN_ID: String
}

object ObjetoJuicioExternalDto {

  case class ObjetoJuicioAnt(
                        EV_ID: String,
                        OJU_SOJ_IDENTIFICADOR: String,
                        OJU_SOJ_TIPO_OBJETO: String,
                        OJU_JUI_ID: String,
                        OJU_PLAN_ID: String
                      ) extends ObjetoJuicioExternalDto with CbroSerialization

  case class ObjetoJuicioTri(
                              EV_ID: String,
                              OJU_SOJ_IDENTIFICADOR: String,
                              OJU_SOJ_TIPO_OBJETO: String,
                              OJU_JUI_ID: String,
                              OJU_PLAN_ID: String
                      ) extends ObjetoJuicioExternalDto with CbroSerialization


}
