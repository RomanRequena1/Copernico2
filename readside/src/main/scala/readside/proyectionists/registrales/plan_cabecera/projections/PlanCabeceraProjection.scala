package readside.proyectionists.registrales.plan_cabecera.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents

trait PlanCabeceraProjection extends UpdateReadSideProjection[PlanCabeceraEvents]{
  def collectionName: String = "read_side.buc_plan_cabecera"

  val keys: List[(String, Object)] = List(
    "bpl_identificador" -> event.planCabeceraId
  )

}
