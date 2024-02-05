package readside.proyectionists.no_registrales.transferencia.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent

trait TransferenciaProjection extends UpdateReadSideProjection[ObjetoVinculoEvent]{
  def collectionName: String = "read_side.buc_objeto_vinculo"
  val keys: List[(String, Object)] = List(
    "soj_suj_identificador" -> event.objetoId
  )
}
