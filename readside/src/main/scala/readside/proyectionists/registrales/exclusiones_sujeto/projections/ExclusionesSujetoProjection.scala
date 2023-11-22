package readside.proyectionists.registrales.exclusiones_sujeto.projections

import cassandra.CassandraTypesAdapter.int
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents

trait ExclusionesSujetoProjection extends UpdateReadSideProjection[ExclusionesSujetoEvents] {
  def collectionName: String = "read_side.buc_exclusiones_sujeto"

  val keys: List[(String, Object)] = List(
    "bep_suj_id" -> int(BigInt(event.sujetoId))
  )
}
