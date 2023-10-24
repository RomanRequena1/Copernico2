package consumers.registral.juicio_obn.application.entities

import play.api.libs.json.JsObject

import java.time.LocalDateTime

case class JuicioObnTri(

    EV_ID: BigInt,
    BJU_IDENTIFICADOR: String,
    BJD_SOJ_TIPO_OBJETO: String,
    BJD_SOJ_IDENTIFICADOR: String,
    BJD_OBN_ID: String,
    BJD_BOB_PERIODO: Option[String],
    BJD_BOB_CUOTA: Option[String],
    BJD_BOB_IMPUESTO: Option[String],
    BJD_BOB_CONCEPTO: Option[String],
    BJD_CANAL_ORIGEN: Option[String],
    BJD_BOB_SALDO: Option[BigDecimal],
    BJD_BOB_ESTADO: Option[String],
    BJD_BOB_CAPITAL: Option[BigDecimal],
    BJD_BOB_VENCIMIENTO: Option[LocalDateTime],
    BJD_BOB_PRORROGA: Option[LocalDateTime],
    BJD_BOB_TIPO: Option[String],
    BJD_BOB_OGA_ID: Option[String],
    BJD_SOJ_ID_EXTERNO: Option[String],
    BJD_BOB_JUI_ID: Option[String],
    BJD_BOB_SUJ_IDENTIFICADOR: Option[String],
    BJD_OTROS_ATRIBUTOS: Option[ListDetallesJuicioTri],
    TEST: Option[String]
                         )

case class DetallesJuicioTri(
                               RULE_NUMBER: Option[String]
                             )

case class ListDetallesJuicioTri(BJD_DETALLES: List[DetallesJuicioTri])