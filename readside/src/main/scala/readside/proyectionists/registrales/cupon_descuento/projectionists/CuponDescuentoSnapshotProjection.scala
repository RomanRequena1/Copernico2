package readside.proyectionists.registrales.cupon_descuento.projectionists

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents

final case class CuponDescuentoSnapshotProjection(
    event: CuponDescuentoEvents.CuponDescuentoPersistedSnapshot
) extends CuponDescuentoProjection {

  val registro: Option[CuponDescuentoExternalDto] = event.registro

  val fromRegistro: Option[List[(String, Option[Serializable])]] = registro map { registro =>
    List(
      "bob_canal_origen" -> registro.BOB_CANAL_ORIGEN,
      "bob_otros_atributos" -> registro.BOB_OTROS_ATRIBUTOS,
      "bob_soj_identificador_2" -> registro.BOB_SOJ_IDENTIFICADOR_2,
    )
  }

  val bindings: List[(String, Serializable)] = fromRegistro.get
}
