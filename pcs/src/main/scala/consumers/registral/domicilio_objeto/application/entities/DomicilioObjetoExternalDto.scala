package consumers.registral.domicilio_objeto.application.entities

import serialization.CbroSerialization


case class DomicilioObjetoTri(EV_ID: String,
                                BDO_SUJ_IDENTIFICADOR: String,
                                BDO_SOJ_TIPO_OBJETO: String,
                                BDO_SOJ_IDENTIFICADOR: String,
                                BDO_DOM_ID: String,
                                BDO_BARRIO: Option[String],
                                BDO_CALLE: Option[String],
                                BDO_CODIGO_POSTAL: Option[String],
                                BDO_DPTO: Option[String],
                                BDO_ESTADO: Option[String],
                                BDO_KILOMETRO: Option[String],
                                BDO_LOCALIDAD: Option[String],
                                BDO_LOTE: Option[String],
                                BDO_MANZANA: Option[String],
                                BDO_PISO: Option[String],
                                BDO_PROVINCIA: Option[String],
                                BDO_PUERTA: Option[String],
                                BDO_TIPO: Option[String],
                                BDO_TORRE: Option[String],
                                BDO_OBSERVACIONES: Option[String])
      extends  CbroSerialization

  case class DomicilioObjetoAnt(EV_ID: String,
                                BDO_SUJ_IDENTIFICADOR: String,
                                BDO_SOJ_TIPO_OBJETO: String,
                                BDO_SOJ_IDENTIFICADOR: String,
                                BDO_DOM_ID: String,
                                BDO_BARRIO: Option[String],
                                BDO_CALLE: Option[String],
                                BDO_CODIGO_POSTAL: Option[String],
                                BDO_DPTO: Option[String],
                                BDO_ESTADO: Option[String],
                                BDO_KILOMETRO: Option[String],
                                BDO_LOCALIDAD: Option[String],
                                BDO_LOTE: Option[String],
                                BDO_MANZANA: Option[String],
                                BDO_PISO: Option[String],
                                BDO_PROVINCIA: Option[String],
                                BDO_PUERTA: Option[String],
                                BDO_TIPO: Option[String],
                                BDO_TORRE: Option[String],
                                BDO_OBSERVACIONES: Option[String])
      extends CbroSerialization
