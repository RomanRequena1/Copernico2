package consumers.registral.exclusiones_sujeto.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[ExclusionesSujetoTri], name = "exclusionesSujetoTri"),
    new JsonSubTypes.Type(value = classOf[ExclusionesSujetoAnt], name = "exclusionSujetoAnt"),
  )
)
sealed trait ExclusionesSujetoExternalDto extends ddd.ExternalDto with CbroSerialization {

  def EV_ID: BigInt

  def BSE_SUJ_IDENTIFICADOR: String

  def BSE_ACCION: Option[String]

  def BSE_MOTIVO: Option[String]

  def BSE_FECHA_DESDE: Option[LocalDateTime]

  def BSE_FECHA_HASTA: Option[LocalDateTime]

  def BSE_ORIGEN: Option[String]

  def BSE_TRAMITE_ID: Option[String]

  def BSE_NOMBRE_GESTION: Option[String]

  def BSE_NUMERO_GESTION: Option[String]

  def BSE_TIPO_EXCLUSION: Option[String]

}


  case class ExclusionesSujetoTri(EV_ID: BigInt,
                                BSE_SUJ_IDENTIFICADOR: String,
                                BSE_ACCION: Option[String], BSE_MOTIVO: Option[String],
                                BSE_FECHA_DESDE: Option[LocalDateTime],
                                BSE_FECHA_HASTA: Option[LocalDateTime],
                                BSE_ORIGEN: Option[String],
                                BSE_TRAMITE_ID: Option[String],
                                BSE_NOMBRE_GESTION: Option[String],
                                BSE_NUMERO_GESTION: Option[String],
                                BSE_TIPO_EXCLUSION: Option[String])
    extends ExclusionesSujetoExternalDto with CbroSerialization


  case class ExclusionesSujetoAnt(EV_ID: BigInt,
                                BSE_SUJ_IDENTIFICADOR: String,
                                BSE_ACCION: Option[String], BSE_MOTIVO: Option[String],
                                BSE_FECHA_DESDE: Option[LocalDateTime],
                                BSE_FECHA_HASTA: Option[LocalDateTime],
                                BSE_ORIGEN: Option[String],
                                BSE_TRAMITE_ID: Option[String],
                                BSE_NOMBRE_GESTION: Option[String],
                                BSE_NUMERO_GESTION: Option[String],
                                BSE_TIPO_EXCLUSION: Option[String])
    extends ExclusionesSujetoExternalDto with CbroSerialization

