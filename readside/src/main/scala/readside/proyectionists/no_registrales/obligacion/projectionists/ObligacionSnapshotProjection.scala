package readside.proyectionists.no_registrales.obligacion.projectionists
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ObligacionExternalDto}
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import io.circe.parser._
import io.circe.syntax.EncoderOps
final case class ObligacionSnapshotProjection(
    event: ObligacionEvents.ObligacionPersistedSnapshot
) extends ObligacionProjection {

  val registro: Option[ObligacionExternalDto] = event.registro

  val bobDetailsResult: Option[Map[String, List[DetallesObligacion]]] =
    decode[Map[String, List[DetallesObligacion]]](registro.get.BOB_OTROS_ATRIBUTOS.asJson.toString()).toOption

  val mao = bobDetailsResult match {
    case Some(value) => Map("BOB_DETALLES" -> value.get("BOB_DETALLES").asJson.noSpaces)
    case None => None
  }

  val fromRegistro = registro map { registro =>
    List(
      "bob_adherido_debito" -> registro.BOB_ADHERIDO_DEBITO,
      "bob_canal_origen" -> registro.BOB_CANAL_ORIGEN,
      "bob_capital" -> registro.BOB_CAPITAL,
      "bob_cuota" -> registro.BOB_CUOTA,
      "bob_estado" -> registro.BOB_ESTADO,
      "bob_concepto" -> registro.BOB_CONCEPTO,
      "bob_fechasancion" -> registro.BOB_FECHASANCION,
      "bob_sub_estado" -> registro.BOB_SUB_ESTADO,
      "bob_tpbid" -> registro.BOB_TPBID,
      "bob_fiscalizada" -> registro.BOB_FISCALIZADA,
      "bob_impuesto" -> registro.BOB_IMPUESTO,
      "bob_interes_punit" -> registro.BOB_INTERES_PUNIT,
      "bob_interes_resar" -> registro.BOB_INTERES_RESAR,
      "bob_jui_id" -> registro.BOB_JUI_ID,
      "bob_otros_atributos" -> Some(mao),
      "bob_periodo" -> registro.BOB_PERIODO,
      "bob_pln_id" -> registro.BOB_PLN_ID,
      "bob_prorroga" -> registro.BOB_PRORROGA,
      "bob_soj_identificador_2" -> registro.BOB_SOJ_IDENTIFICADOR_2,
      "bob_tipo" -> registro.BOB_TIPO,
      "bob_total" -> registro.BOB_TOTAL,
      "bob_vencimiento" -> registro.BOB_VENCIMIENTO,
      "bob_oga_id" -> registro.BOB_OGA_ID,
      "bob_vencimiento_2" -> registro.BOB_VENCIMIENTO_2,
      "bob_deuda30Obligacion" -> registro.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map(m => m.deuda30Obligaciones),
      "bob_resultDmn" -> event.resultDmn

    )
  }
  val other: List[(String, BigDecimal)] =
    List(
      "bob_saldo" -> event.saldo
      //"bob_porcentaje_exencion" -> event.porcentajeExencion,
      //"bob_exenta" -> event.exenta
    )
  val bindings: List[(String, Serializable)] = fromRegistro match {
    case Some(fromRegistro) => fromRegistro ++ other
    case None => other
  }
}
