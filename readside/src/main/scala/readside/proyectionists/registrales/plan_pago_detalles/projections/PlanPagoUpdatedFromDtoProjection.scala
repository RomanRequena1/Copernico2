package readside.proyectionists.registrales.plan_pago_detalles.projections
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoExternalDto
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents
case class PlanPagoUpdatedFromDtoProjection(
    event: PlanPagoEvents.PlanPagoUpdatedFromDto
) extends PlanPagoProjection {
  val registro: PlanPagoExternalDto = event.registro

/*
  val bobDetailsResult: Option[Map[String, List[DetallePlanPago]]] =
    decode[Map[String, List[DetallePlanPago]]](registro.BPL_OTROS_ATRIBUTOS.asJson.toString()).toOption
  println("CUMBIA bobDetailsResult -> " + bobDetailsResult)

  val mao: Map[String, String] = Map("BPL_DETALLES" -> bobDetailsResult.get("BPL_DETALLES").asJson.noSpaces)
  println("CUMBIA -> mao" + mao)*/


  def bindings: List[(String, Serializable)] = List(
    "bpd_bob_periodo" -> registro.BPD_BOB_PERIODO,
    "bpd_bob_cuota" -> registro.BPD_BOB_CUOTA,
    "bpd_bob_impuesto" -> registro.BPD_BOB_IMPUESTO,
    "bpd_bob_concepto" -> registro.BPD_BOB_CONCEPTO,
    "bpd_canal_origen" -> registro.BPD_CANAL_ORIGEN,
    "bpd_estado" -> registro.BPD_ESTADO,
    "rule_number" -> registro.RULE_NUMBER,
    "bpd_bob_importe_a_financiar" -> registro.BPD_BOB_IMPORTE_A_FINANCIAR,
    "bpd_bob_vencimiento" -> registro.BPD_BOB_VENCIMIENTO,
    "bpd_bob_prorroga" -> registro.BPD_BOB_PRORROGA,
    "bpd_bob_tipo" -> registro.BPD_BOB_TIPO,
    "bpd_bob_oga_id" -> registro.BPD_BOB_OGA_ID,
    "bpd_bju_identificador" -> registro.BPD_BJU_IDENTIFICADOR,


    //"bpl_otros_atributos" -> registro.BPL_OTROS_ATRIBUTOS
  )
}
