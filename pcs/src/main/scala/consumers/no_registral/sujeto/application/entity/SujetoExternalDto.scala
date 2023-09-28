package consumers.no_registral.sujeto.application.entity

import ddd.ExternalDto
import serialization.CbroSerialization





  case class SujetoAnt(
      EV_ID: BigInt,
      SUJ_IDENTIFICADOR: String,
      SUJ_CAT_SUJ_ID: Option[BigInt],
      SUJ_DENOMINACION: Option[String],
      SUJ_DFE: Option[String],
      SUJ_DIRECCION: Option[String],
      SUJ_EMAIL: Option[String],
      SUJ_ID_EXTERNO: Option[String],
      SUJ_OTROS_ATRIBUTOS: Option[Map[String, String]],
      SUJ_RIESGO_FISCAL: Option[String],
      SUJ_SITUACION_FISCAL: Option[String],
      SUJ_TELEFONO: Option[String],
      SUJ_TIPO: Option[String],
      SUJ_CANAL_ORIGEN: Option[String]
  ) extends CbroSerialization

  case class SujetoTri(
      EV_ID: BigInt,
      SUJ_IDENTIFICADOR: String,
      SUJ_CAT_SUJ_ID: Option[BigInt],
      SUJ_DENOMINACION: Option[String],
      SUJ_DFE: Option[String],
      SUJ_DIRECCION: Option[String],
      SUJ_EMAIL: Option[String],
      SUJ_ID_EXTERNO: Option[String],
      SUJ_OTROS_ATRIBUTOS: Option[Map[String, String]],
      SUJ_RIESGO_FISCAL: Option[String],
      SUJ_SITUACION_FISCAL: Option[String],
      SUJ_TELEFONO: Option[String],
      SUJ_TIPO: Option[String],
      SUJ_CANAL_ORIGEN: Option[String]
  ) extends CbroSerialization


