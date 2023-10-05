package consumers.no_registral.objeto.application.entities

import java.time.LocalDateTime
import serialization.CbroSerialization

  case class ObjetosAnt(
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
      SOJ_OTROS_ATRIBUTOS: Option[ListObjetosTriOtrosAtributos],
      SOJ_BASE_IMPONIBLE: Option[BigDecimal],
      SOJ_ADHERIDO_DEBITO: Option[String],
      SOJ_CANT_CUOTAS_PAGADAS: Option[BigInt],
      SOJ_CANAL_ORIGEN: Option[String],
      SOJ_SUBTIPO: Option[String],
      SOJ_IDENTIFICADOR_2: Option[String],
      SOJ_TITULARIDAD: Option[String]
  ) extends CbroSerialization

case class ListObjetosTriOtrosAtributos(SOJ_DETALLES: List[ObjetosTriOtrosAtributos]) extends CbroSerialization
case class ObjetosTriOtrosAtributos(
      RESPONSABLE_OTROS_ATRIBUTOS: Option[String],
      PORCENTAJE_OTROS_ATRIBUTOS: Option[BigDecimal],
      CUENTA_SOJ_OTROS_ATRIBUTOS: Option[String],
      OTROS_ATRIBUTOS_ADHERIDO_DEBITO: Option[String],
      PERIODO_SOJ_OTROS_ATRIBUTOS: Option[String],
      IMPORTE_SOJ_OTROS_ATRIBUTOS: Option[String],
      SOJ_SEMAFORO_COLOR: Option[String],
      SOJ_SEMAFORO_MARCA: Option[String]
  ) extends CbroSerialization

case class ObjetosTri(
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
      SOJ_OTROS_ATRIBUTOS: Option[ListObjetosTriOtrosAtributos],
      SOJ_BASE_IMPONIBLE: Option[BigDecimal],
      SOJ_ADHERIDO_DEBITO: Option[String],
      SOJ_CANT_CUOTAS_PAGADAS: Option[BigInt],
      SOJ_CANAL_ORIGEN: Option[String],
      SOJ_SUBTIPO: Option[String],
      SOJ_IDENTIFICADOR_2: Option[String],
      SOJ_TITULARIDAD: Option[String]
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

