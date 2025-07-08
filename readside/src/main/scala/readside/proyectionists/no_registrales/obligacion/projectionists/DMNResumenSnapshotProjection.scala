package readside.proyectionists.no_registrales.dmn.projectionists

import cassandra.mechanism.UpdateReadSideProjection
import consumers.no_registral.obligacion.domain.ObligacionEvents.DMNResumenPersisted

final case class DMNResumenSnapshotProjection(event: DMNResumenPersisted)
  extends UpdateReadSideProjection[DMNResumenPersisted] {

  def collectionName: String = "read_side.dmn_resumen"

  val keys: List[(String, Any)] = List(
    "sujeto_id" -> event.sujetoId,
    "objeto_id" -> event.objetoId,
    "tipo_objeto" -> event.tipoObjeto,
    "obligacion_id" -> event.obligacionId
  )

  val bindings: List[(String, Serializable)] = List(
    "dmn_numero" -> event.dmnNumero,
    "dmn_descripcion" -> event.dmnDescripcion
  )
}
