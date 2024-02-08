package readside.proyectionists.no_registrales.transferencia.projections

import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent.ObjetoVinculoSnapshotPersisted

case class TransferenciaSnapshotPersistedProjection(
    event: ObjetoVinculoSnapshotPersisted
) extends TransferenciaProjection{

  def bindings: List[(String, Option[String])] = List("soj_tipo_objeto" -> Some(event.tipoObj),
    "soj_map_transf" -> Some(event.mapTransf.toString()),
    "soj_map_vinculo" -> Some(event.mapVinculo.toString())
  )

}

