package consumers.no_registral.objeto.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{ListDetallesObjeto, ObjetosAnt, ObjetosTri}
import ddd.ExternalDto
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[ObjetosTri], name = "objetosTri"),
    new JsonSubTypes.Type(value = classOf[ObjetosAnt], name = "objetosAnt"),
  )
)
sealed trait ObjetoExternalDto extends ExternalDto with CbroSerialization{
  def RULE_NUMBER: Option[String]

  def EV_ID: BigInt

  def SOJ_SUJ_IDENTIFICADOR: String

  def SOJ_TIPO_OBJETO: String

  def SOJ_IDENTIFICADOR: String

  def SOJ_CAT_SOJ_ID: Option[String]

  def SOJ_DESCRIPCION: Option[String]

  def SOJ_ESTADO: Option[String]

  def SOJ_FECHA_INICIO: Option[LocalDateTime]

  def SOJ_FECHA_FIN: Option[LocalDateTime]

  def SOJ_ID_EXTERNO: Option[String]

  def SOJ_OTROS_ATRIBUTOS: Option[ListDetallesObjeto]

  def SOJ_BASE_IMPONIBLE: Option[BigDecimal]

  def SOJ_ADHERIDO_DEBITO: Option[String]

  def SOJ_CANT_CUOTAS_PAGADAS: Option[BigInt]

  def SOJ_CANAL_ORIGEN: Option[String]

  def SOJ_SUBTIPO: Option[String]

  def SOJ_IDENTIFICADOR_2: Option[String]

  def SOJ_TITULARIDAD: Option[String]

}


object ObjetoExternalDto{
  case class ObjetosAnt(
                         RULE_NUMBER: Option[String],
                         EV_ID: BigInt,
                         SOJ_SUJ_IDENTIFICADOR: String,
                         SOJ_TIPO_OBJETO: String,
                         SOJ_IDENTIFICADOR: String,
                         SOJ_CAT_SOJ_ID: Option[String],
                         SOJ_DESCRIPCION: Option[String],
                         SOJ_ESTADO: Option[String],
                         SOJ_FECHA_INICIO: Option[LocalDateTime],
                         SOJ_FECHA_FIN: Option[LocalDateTime],
                         SOJ_ID_EXTERNO: Option[String],
                         SOJ_OTROS_ATRIBUTOS: Option[ListDetallesObjeto],
                         SOJ_BASE_IMPONIBLE: Option[BigDecimal],
                         SOJ_ADHERIDO_DEBITO: Option[String],
                         SOJ_CANT_CUOTAS_PAGADAS: Option[BigInt],
                         SOJ_CANAL_ORIGEN: Option[String],
                         SOJ_SUBTIPO: Option[String],
                         SOJ_IDENTIFICADOR_2: Option[String],
                         SOJ_TITULARIDAD: Option[String]
                       ) extends ObjetoExternalDto with CbroSerialization

  case class ObjetosTri(
                         RULE_NUMBER: Option[String],
                         EV_ID: BigInt,
                         SOJ_SUJ_IDENTIFICADOR: String,
                         SOJ_TIPO_OBJETO: String,
                         SOJ_IDENTIFICADOR: String,
                         SOJ_CAT_SOJ_ID: Option[String],
                         SOJ_DESCRIPCION: Option[String],
                         SOJ_ESTADO: Option[String],
                         SOJ_FECHA_INICIO: Option[LocalDateTime],
                         SOJ_FECHA_FIN: Option[LocalDateTime],
                         SOJ_ID_EXTERNO: Option[String],
                         SOJ_OTROS_ATRIBUTOS: Option[ListDetallesObjeto],
                         SOJ_BASE_IMPONIBLE: Option[BigDecimal],
                         SOJ_ADHERIDO_DEBITO: Option[String],
                         SOJ_CANT_CUOTAS_PAGADAS: Option[BigInt],
                         SOJ_CANAL_ORIGEN: Option[String],
                         SOJ_SUBTIPO: Option[String],
                         SOJ_IDENTIFICADOR_2: Option[String],
                         SOJ_TITULARIDAD: Option[String]
                       ) extends ObjetoExternalDto with CbroSerialization

  case class ListDetallesObjeto(SOJ_DETALLES: List[DetallesObjeto]) extends CbroSerialization

  case class DetallesObjeto(
                             RESPONSABLE_OTROS_ATRIBUTOS: Option[String],
                             PORCENTAJE_OTROS_ATRIBUTOS: Option[BigDecimal],
                             CUENTA_SOJ_OTROS_ATRIBUTOS: Option[String],
                             OTROS_ATRIBUTOS_ADHERIDO_DEBITO: Option[String],
                             PERIODO_SOJ_OTROS_ATRIBUTOS: Option[String],
                             IMPORTE_SOJ_OTROS_ATRIBUTOS: Option[String],
                             SOJ_SEMAFORO_COLOR: Option[String],
                             SOJ_SEMAFORO_MARCA: Option[String]
                           ) extends CbroSerialization

  case class Cotitularidad(
                            SOJ_SUJ_IDENTIFICADOR: String,
                            SOJ_IDENTIFICADOR: String,
                            SOJ_TIPO_OBJETO: String,
                            EV_ID: BigInt,
                            RESPONSABLE: String,
                            REAL_RESPONSABLE: String,
                            PORCENTAJE_RESPONSABILIDAD: BigDecimal,
                            COTITULARES: Set[String]
                          ) extends CbroSerialization

  case class Exencion(
                       EV_ID: BigInt,
                       BEX_SUJ_IDENTIFICADOR: String,
                       BEX_SOJ_IDENTIFICADOR: String,
                       BEX_EXE_ID: String,
                       BEX_SOJ_TIPO_OBJETO: String,
                       BEX_DESCRIPCION: Option[String],
                       BEX_FECHA_INICIO: Option[LocalDateTime],
                       BEX_FECHA_FIN: Option[LocalDateTime],
                       BEX_PERIODO: Option[String],
                       BEX_PORCENTAJE: Option[BigDecimal],
                       BEX_TIPO: Option[String]
                     ) extends CbroSerialization
}

