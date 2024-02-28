package readside.proyectionists.registrales.plan_pago_detalles.projections
import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents

trait PlanPagoProjection extends UpdateReadSideProjection[PlanPagoEvents] {
  def collectionName: String = "read_side.buc_plan_pago_detalles"
  val keys: List[(String, Object)] = List(
    "bpl_identificador" -> event.planPagoId,
    "bpd_soj_tipo_objeto" -> event.tipoObjeto,
    "bpd_soj_identificador" -> event.objetoId,
    "bpd_obn_id" -> event.obligacionId
  )
}
