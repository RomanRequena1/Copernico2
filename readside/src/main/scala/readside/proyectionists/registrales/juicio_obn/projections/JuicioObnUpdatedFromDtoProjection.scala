package readside.proyectionists.registrales.juicio_obn.projections

import consumers.registral.juicio_obn.application.entities.{DetallesJuicioTri, JuicioObnTri}
import consumers.registral.juicio_obn.domain.JuicioObnEvents
import consumers.registral.juicio_obn.infrastructure.json.json._
import io.circe.syntax.EncoderOps
import io.circe.parser._


case class JuicioObnUpdatedFromDtoProjection(
    event: JuicioObnEvents.JuicioObnUpdatedFromDto
) extends JuicioObnProjection {
  val registro: JuicioObnTri = event.registro


  val bobDetailsResult: Option[Map[String, List[DetallesJuicioTri]]] = {
    decode[Map[String, List[DetallesJuicioTri]]](registro.BJD_OTROS_ATRIBUTOS.asJson.toString()).toOption
  }
  val mao: Map[String, String] = Map("BJU_DETALLES" -> bobDetailsResult.get("BJU_DETALLES").asJson.noSpaces)


  def bindings: List[(String, Serializable)] = List(
    "bjd_bob_periodo" -> registro.BJD_BOB_PERIODO,
    "bjd_bob_cuota" -> registro.BJD_BOB_CUOTA,
    "bjd_bob_impuesto" -> registro.BJD_BOB_IMPUESTO,
    "bjd_bob_concepto" -> registro.BJD_BOB_CONCEPTO,
    "bjd_canal_origen" -> registro.BJD_CANAL_ORIGEN,
    "bjd_bob_saldo" -> registro.BJD_BOB_SALDO,
    "bjd_bob_estado" -> registro.BJD_BOB_ESTADO,
    "bjd_bob_capital" -> registro.BJD_BOB_CAPITAL,
    "bjd_bob_vencimiento" -> registro.BJD_BOB_VENCIMIENTO,
    "bjd_bob_prorroga" -> registro.BJD_BOB_PRORROGA,
    "bjd_bob_tipo" -> registro.BJD_BOB_TIPO,
    "bjd_bob_oga_id" -> registro.BJD_BOB_OGA_ID,
    "bjd_soj_id_externo" -> registro.BJD_SOJ_ID_EXTERNO,
    "bjd_bob_jui_id" -> registro.BJD_BOB_JUI_ID,
    "bjd_bob_suj_identificador" -> registro.BJD_BOB_SUJ_IDENTIFICADOR,
    "bjd_otros_atributos" -> registro.BJD_OTROS_ATRIBUTOS,
  )
}
