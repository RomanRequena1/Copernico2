package readside.proyectionists.registrales.componente_i.projectionists

import consumers.registral.componente_i.application.entities.ComponenteIExternalDto
import consumers.registral.componente_i.domain.ComponenteIEvents

final case class ComponenteISnapshotProjection(
    event: ComponenteIEvents.ComponenteIPersistedSnapshot
) extends ComponenteIProjection {

  val registro: Option[ComponenteIExternalDto] = event.registro

  val fromRegistro: Option[List[(String, Option[Serializable])]] = registro map { registro =>
    List(
      "bci_canal_origen" -> registro.BOB_CANAL_ORIGEN,
      "bci_otros_atributos" -> registro.BOB_OTROS_ATRIBUTOS,
      "bci_soj_identificador_2" -> registro.BOB_SOJ_IDENTIFICADOR_2,
    )
  }

  val bindings: List[(String, Serializable)] = fromRegistro.get
}
