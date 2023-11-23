package readside.proyectionists.registrales.exclusiones_sujeto.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents

trait ExclusionesSujetoProjection extends UpdateReadSideProjection[ExclusionesSujetoEvents] {
  def collectionName: String = "read_side.buc_exclusiones_sujeto"

  val keys: List[(String, Object)] = List(
    "bse_suj_id" -> event.sujetoId
  )
}
