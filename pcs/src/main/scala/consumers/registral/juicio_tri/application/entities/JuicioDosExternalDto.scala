package consumers.registral.juicio_tri.application.entities

import java.time.LocalDateTime


sealed trait JuicioDosExternalDto extends ddd.ExternalDto{
  def EV_ID: String
  def BJU_IDENTIFICADOR: String
  def BJU_JUI_ID: Option[String]
  def BJU_SUJ_IDENTIFICADOR: Option[String]
  def BJU_NRO_EXTERNO: Option[String]
  def BJU_CAPITAL: Option[BigDecimal]
  def BJU_TOTAL: Option[BigDecimal]
  def BJU_ESTADO: Option[String]
  def BJU_FECHA_GENERACION: Option[LocalDateTime]
  def BJU_FECHA_IMPRESION: Option[LocalDateTime]
  def BJU_TIPO: Option[String]
  def BJU_TIPO_JUICIO: Option[String]
  def BJU_CUIT_ORIGEN: Option[String]
  def BJU_SOJ_TIPO_OBJETO: Option[String]
  def BJU_SOJ_IDENTIFICADOR: Option[String]
  def BJU_IPO_ID: Option[String]
  def BJU_CANAL_ORIGEN: Option[String]
}

object JuicioDosExternalDto {
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
                      ) extends JuicioDosExternalDto
}
