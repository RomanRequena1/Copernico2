package consumers.registral.etapas_procesales.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.{SujetoAnt, SujetoTri}
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto.DetalleEtapasProcesales
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[SujetoTri], name = "etapasProcesalesTri"),
    new JsonSubTypes.Type(value = classOf[SujetoAnt], name = "etapasProcesalesAnt"),
  )
)
sealed trait EtapasProcesalesExternalDto extends ddd.ExternalDto with CbroSerialization {

  def EV_ID: String

  def BEP_JUI_ID: String

  def BPE_ETA_ID: String

  def BEP_DESCRIPCION: Option[String]

  def BEP_FECHA_FIN: Option[LocalDateTime]

  def BEP_FECHA_INICIO: Option[LocalDateTime]

  def BEP_OTROS_ATRIBUTOS: Option[DetalleEtapasProcesales]

  def BEP_REFERENCIA: Option[String]

  def BEP_TIPO: Option[String]

}

object EtapasProcesalesExternalDto {


  case class EtapasProcesalesTri(EV_ID: String,
                                 BEP_JUI_ID: String,
                                 BPE_ETA_ID: String,
                                 BEP_DESCRIPCION: Option[String],
                                 BEP_FECHA_FIN: Option[LocalDateTime],
                                 BEP_FECHA_INICIO: Option[LocalDateTime],
                                 BEP_OTROS_ATRIBUTOS: Option[DetalleEtapasProcesales],
                                 BEP_REFERENCIA: Option[String],
                                 BEP_TIPO: Option[String])
    extends EtapasProcesalesExternalDto with CbroSerialization

  case class DetalleEtapasProcesales(BEP_DETALLES: Option[String]) extends CbroSerialization

  case class EtapasProcesalesAnt(EV_ID: String,
                                 BEP_JUI_ID: String,
                                 BPE_ETA_ID: String,
                                 BEP_DESCRIPCION: Option[String],
                                 BEP_FECHA_FIN: Option[LocalDateTime],
                                 BEP_FECHA_INICIO: Option[LocalDateTime],
                                 BEP_OTROS_ATRIBUTOS: Option[DetalleEtapasProcesales],
                                 BEP_REFERENCIA: Option[String],
                                 BEP_TIPO: Option[String])
    extends EtapasProcesalesExternalDto with CbroSerialization
}
