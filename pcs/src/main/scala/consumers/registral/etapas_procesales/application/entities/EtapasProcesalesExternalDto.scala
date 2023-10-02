package consumers.registral.etapas_procesales.application.entities

import java.time.LocalDateTime
import play.api.libs.json.JsObject
import serialization.CbroSerialization



  case class EtapasProcesalesTri(EV_ID: String,
                                 BEP_JUI_ID: String,
                                 BPE_ETA_ID: String,
                                 BEP_DESCRIPCION: Option[String],
                                 BEP_FECHA_FIN: Option[LocalDateTime],
                                 BEP_FECHA_INICIO: Option[LocalDateTime],
                                 BEP_OTROS_ATRIBUTOS: DetalleEtapasProcesalesTri,
                                 BEP_REFERENCIA: Option[String],
                                 BEP_TIPO: Option[String])
      extends CbroSerialization
case class DetalleEtapasProcesalesTri(BEP_DETALLES: String) extends CbroSerialization
  case class EtapasProcesalesAnt(EV_ID: String,
                                 BEP_JUI_ID: String,
                                 BPE_ETA_ID: String,
                                 BEP_DESCRIPCION: Option[String],
                                 BEP_FECHA_FIN: Option[LocalDateTime],
                                 BEP_FECHA_INICIO: Option[LocalDateTime],
                                 BEP_OTROS_ATRIBUTOS: DetalleEtapasProcesalesTri,
                                 BEP_REFERENCIA: Option[String],
                                 BEP_TIPO: Option[String])
      extends CbroSerialization
