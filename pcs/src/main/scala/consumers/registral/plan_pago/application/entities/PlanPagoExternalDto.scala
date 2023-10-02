package consumers.registral.plan_pago.application.entities

import java.time.LocalDateTime
import play.api.libs.json.JsObject
import serialization.CbroSerialization


  case class PlanPagoTri(EV_ID: String,
                         BPL_SUJ_IDENTIFICADOR: String,
                         BPL_SOJ_TIPO_OBJETO: String,
                         BPL_SOJ_IDENTIFICADOR: String,
                         BPL_PLN_ID: String,
                         BPL_CANTIDAD_CUOTAS: Option[BigInt],
                         BPL_ESTADO: Option[String],
                         BPL_FECHA_ACT_DEUDA: Option[LocalDateTime],
                         BPL_FECHA_EMISION: Option[LocalDateTime],
                         BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                         BPL_IMPORTE_ANTICIPO: Option[BigDecimal],
                         BPL_IMPORTE_FINANCIADO: Option[BigDecimal],
                         BPL_NRO_REFERENCIA: Option[String],
                         BPL_TIPO: Option[String],
                         BPL_OTROS_ATRIBUTOS: Option[ListaDetallePlanPlago])
      extends CbroSerialization

  case class PlanPagoAnt(EV_ID: String,
                         BPL_SUJ_IDENTIFICADOR: String,
                         BPL_SOJ_TIPO_OBJETO: String,
                         BPL_SOJ_IDENTIFICADOR: String,
                         BPL_PLN_ID: String,
                         BPL_CANTIDAD_CUOTAS: Option[BigInt],
                         BPL_ESTADO: Option[String],
                         BPL_FECHA_ACT_DEUDA: Option[LocalDateTime],
                         BPL_FECHA_EMISION: Option[LocalDateTime],
                         BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                         BPL_IMPORTE_ANTICIPO: Option[BigDecimal],
                         BPL_IMPORTE_FINANCIADO: Option[BigDecimal],
                         BPL_NRO_REFERENCIA: Option[String],
                         BPL_TIPO: Option[String],
                         BPL_OTROS_ATRIBUTOS: Option[ListaDetallePlanPlago])
      extends CbroSerialization
case class DetallePlanPago(
    BPL_OBN_ID : Option[String],
    BPL_PERIODO : Option[String],
    BPL_CUOTA : Option[String],
    BPL_VENCIMIENTO : Option[LocalDateTime],
    BPL_OBJETO : Option[String],
    BPL_TIPO_OBJETO : Option[String]
                          ) extends CbroSerialization

case class ListaDetallePlanPlago(BPL_DETALLES: List[DetallePlanPago]) extends CbroSerialization