package consumers.registral.componente_i.application.entities

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import ddd.ExternalDto
import play.api.libs.json.JsObject
import java.time.LocalDateTime

sealed trait ComponenteIExternalDto extends ExternalDto {
  def EV_ID: BigInt
  def BOB_SUJ_IDENTIFICADOR: String
  def BOB_SOJ_TIPO_OBJETO: String
  def BOB_SOJ_IDENTIFICADOR: String
  def BOB_SOJ_IDENTIFICADOR_2: Option[String]
  def BOB_OBN_ID: String
  def BOB_CANAL_ORIGEN: Option[String]
  def BOB_OTROS_ATRIBUTOS: Option[JsObject]

}

object ComponenteIExternalDto {

  case class ComponenteITri(
                              EV_ID: BigInt,
                              BOB_SUJ_IDENTIFICADOR: String,
                              BOB_SOJ_IDENTIFICADOR_2: Option[String],
                              BOB_SOJ_TIPO_OBJETO: String,
                              BOB_SOJ_IDENTIFICADOR: String,
                              BOB_OBN_ID: String,
                              BOB_CANAL_ORIGEN: Option[String],
                              BOB_OTROS_ATRIBUTOS: Option[JsObject],

                            ) extends ComponenteIExternalDto


  case class DetallesComponenteI(
                                 sequence: Option[String],
                                 ruleDescription: Option[String],
                                 amountCalculated:Option[String],
                                 distributionId:Option[String]
                               )
}
