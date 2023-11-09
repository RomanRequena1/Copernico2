package readside.proyectionists.registrales.plan_pago_detalles.projections
import cassandra.CassandraTypesAdapter.int
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents
import cassandra.mechanism.UpdateReadSideProjection

trait PlanPagoProjection extends UpdateReadSideProjection[PlanPagoEvents] {
  def collectionName: String = "read_side.buc_plan_pago_detalles"
  val keys: List[(String, Object)] = List(
    "bpl_identificador" -> int(Some(BigInt(event.planPagoId))),
    "bpd_soj_tipo_objeto" -> event.tipoObjeto,
    "bpd_soj_identificador" -> event.objetoId,
    "bpd_obn_id" -> event.objetoId
  )
}
