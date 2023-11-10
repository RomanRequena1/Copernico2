package readside.proyectionists.registrales.plan_cabecera.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents

trait PlanCabeceraProjection extends UpdateReadSideProjection[PlanCabeceraEvents]{
  def collectionName: String = "read_side.buc_plan_cabecera"

  val keys: List[(String, Object)] = List(
    "bpl_identificador" -> event.planPagoId,
    "bpd_soj_tipo_objeto" -> event.tipoObjeto,
    "bpd_soj_identificador" -> event.objetoId,
    "bpd_obn_id" -> event.obligacionId
  )

}
