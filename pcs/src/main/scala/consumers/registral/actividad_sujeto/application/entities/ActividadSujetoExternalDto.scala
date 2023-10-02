package consumers.registral.actividad_sujeto.application.entities

import java.time.LocalDateTime
import ddd.ExternalDto
import play.api.libs.json.JsObject
import serialization.CbroSerialization


  case class ActividadSujeto(
      EV_ID: String,
      BAT_SUJ_IDENTIFICADOR: String,
      BAT_ATD_ID: String,
      BAT_DESCRIPCION: Option[String],
      BAT_FECHA_INICIO: Option[LocalDateTime],
      BAT_FECHA_FIN: Option[LocalDateTime],
      BAT_OTROS_ATRIBUTOS: DetallesActividadSujeto,
      BAT_REFERENCIA: Option[String],
      BAT_TIPO: Option[String]
  ) extends CbroSerialization

case class DetallesActividadSujeto(
                                  BAT_DETALLE: String
                                  ) extends CbroSerialization
