package consumers.registral.juicio_tri.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime




  case class JuicioDosTri(
                        EV_ID: String,
                        BJU_IDENTIFICADOR: String,
                        BJU_JUI_ID: Option[String],
                        BJU_SUJ_IDENTIFICADOR: Option[String],
                        BJU_NRO_EXTERNO: Option[String],
                        BJU_CAPITAL: Option[BigDecimal],
                        BJU_TOTAL: Option[BigDecimal],
                        BJU_ESTADO: Option[String],
                        BJU_FECHA_GENERACION: Option[LocalDateTime],
                        BJU_FECHA_IMPRESION: Option[LocalDateTime],
                        BJU_TIPO: Option[String],
                        BJU_TIPO_JUICIO: Option[String],
                        BJU_CUIT_ORIGEN: Option[String],
                        BJU_SOJ_TIPO_OBJETO: Option[String],
                        BJU_SOJ_IDENTIFICADOR: Option[String],
                        BJU_IPO_ID: Option[String],
                        BJU_CANAL_ORIGEN: Option[String]
                      ) extends CbroSerialization

