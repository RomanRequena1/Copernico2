package consumers.no_registral.exclusiones_objeto.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[ExclusionesObjetoTri], name = "exclusionesObjetoTri"),
    new JsonSubTypes.Type(value = classOf[ExclusionesObjetoAnt], name = "exclusionesObjetoAnt"),
  )
)
sealed trait ExclusionesObjetoExternalDto extends ddd.ExternalDto with CbroSerialization {

  def EV_ID: BigInt

  def BOE_SOJ_IDENTIFICADOR: String

  def BOE_ACCION: Option[String]

  def BOE_MOTIVO: Option[String]

  def BOE_FECHA_DESDE: Option[LocalDateTime]

  def BOE_FECHA_HASTA: Option[LocalDateTime]

  def BOE_ORIGEN: Option[String]

  def BOE_TRAMITE_ID: Option[String]

  def BOE_NOMBRE_GESTION: Option[String]

  def BOE_NUMERO_GESTION: Option[String]

  def BOE_TIPO_EXCLUSION: Option[String]

}


case class ExclusionesObjetoTri(EV_ID: BigInt,
                                BOE_SOJ_IDENTIFICADOR: String,
                                BOE_ACCION: Option[String],
                                BOE_MOTIVO: Option[String],
                                BOE_FECHA_DESDE: Option[LocalDateTime],
                                BOE_FECHA_HASTA: Option[LocalDateTime],
                                BOE_ORIGEN: Option[String],
                                BOE_TRAMITE_ID: Option[String],
                                BOE_NOMBRE_GESTION: Option[String],
                                BOE_NUMERO_GESTION: Option[String],
                                BOE_TIPO_EXCLUSION: Option[String])
  extends ExclusionesObjetoExternalDto with CbroSerialization


case class ExclusionesObjetoAnt(EV_ID: BigInt,
                                BOE_SOJ_IDENTIFICADOR: String,
                                BOE_ACCION: Option[String],
                                BOE_MOTIVO: Option[String],
                                BOE_FECHA_DESDE: Option[LocalDateTime],
                                BOE_FECHA_HASTA: Option[LocalDateTime],
                                BOE_ORIGEN: Option[String],
                                BOE_TRAMITE_ID: Option[String],
                                BOE_NOMBRE_GESTION: Option[String],
                                BOE_NUMERO_GESTION: Option[String],
                                BOE_TIPO_EXCLUSION: Option[String])
  extends ExclusionesObjetoExternalDto with CbroSerialization
