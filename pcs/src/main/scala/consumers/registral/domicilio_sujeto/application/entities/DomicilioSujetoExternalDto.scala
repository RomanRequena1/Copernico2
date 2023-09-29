package consumers.registral.domicilio_sujeto.application.entities

import serialization.CbroSerialization


case class DomicilioSujetoTri(EV_ID: String,
                                BDS_SUJ_IDENTIFICADOR: String,
                                BDS_DOM_ID: String,
                                BDS_BARRIO: Option[String],
                                BDS_CALLE: Option[String],
                                BDS_CODIGO_POSTAL: Option[String],
                                BDS_DPTO: Option[String],
                                BDS_ESTADO: Option[String],
                                BDS_KILOMETRO: Option[String],
                                BDS_LOCALIDAD: Option[String],
                                BDS_LOTE: Option[String],
                                BDS_MANZANA: Option[String],
                                BDS_PISO: Option[String],
                                BDS_PROVINCIA: Option[String],
                                BDS_PUERTA: Option[String],
                                BDS_TIPO: Option[String],
                                BDS_TORRE: Option[String],
                                BDS_OBSERVACIONES: Option[String])
      extends CbroSerialization

  case class DomicilioSujetoAnt(EV_ID: String,
                                BDS_SUJ_IDENTIFICADOR: String,
                                BDS_DOM_ID: String,
                                BDS_BARRIO: Option[String],
                                BDS_CALLE: Option[String],
                                BDS_CODIGO_POSTAL: Option[String],
                                BDS_DPTO: Option[String],
                                BDS_ESTADO: Option[String],
                                BDS_KILOMETRO: Option[String],
                                BDS_LOCALIDAD: Option[String],
                                BDS_LOTE: Option[String],
                                BDS_MANZANA: Option[String],
                                BDS_PISO: Option[String],
                                BDS_PROVINCIA: Option[String],
                                BDS_PUERTA: Option[String],
                                BDS_TIPO: Option[String],
                                BDS_TORRE: Option[String],
                                BDS_OBSERVACIONES: Option[String])
      extends CbroSerialization


