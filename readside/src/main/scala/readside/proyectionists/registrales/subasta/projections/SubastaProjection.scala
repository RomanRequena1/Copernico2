package readside.proyectionists.registrales.subasta.projections
import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.subasta.domain.SubastaEvents

trait SubastaProjection extends UpdateReadSideProjection[SubastaEvents] {
  def collectionName: String = "read_side.buc_subastas"

  val keys: List[(String, String)] = List(
    "bsb_suj_identificador_adq" -> event.sujetoId,
    "bsb_soj_tipo_objeto" -> event.tipoObjeto,
    "bsb_soj_identificador" -> event.objetoId,
    "bsb_sub_id" -> event.subastaId
  )
}
