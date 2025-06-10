package consumers.registral.objeto_juicio.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.ObjetoJuicioTri
import serialization.CbroSerialization

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[ObjetoJuicioTri], name = "ObjetoJuicioTri"),
  )
)
sealed trait ObjetoJuicioExternalDto extends ddd.ExternalDto with CbroSerialization{
  def EV_ID: BigInt
  def RJP_SOJ_IDENTIFICADOR: String
  def RJP_SOJ_TIPO_OBJETO: String
  def RJP_IDENTIFICADOR_REL: String
  def RJP_TIPO_OBJETO_REL: String
  def RJP_TIPO_REL: String
  def RJP_ID_EXTERNO: String
  def RJP_ID_EXTERNO_2: String
  def RJP_ESTADO: String
}

object ObjetoJuicioExternalDto {

  case class ObjetoJuicioTri(
                              EV_ID: BigInt,
                              RJP_SOJ_IDENTIFICADOR: String,
                              RJP_SOJ_TIPO_OBJETO: String,
                              RJP_IDENTIFICADOR_REL: String,
                              RJP_TIPO_OBJETO_REL: String,
                              RJP_TIPO_REL: String,
                              RJP_ID_EXTERNO: String,
                              RJP_ID_EXTERNO_2: String,
                              RJP_ESTADO: String
                      ) extends ObjetoJuicioExternalDto with CbroSerialization
}
