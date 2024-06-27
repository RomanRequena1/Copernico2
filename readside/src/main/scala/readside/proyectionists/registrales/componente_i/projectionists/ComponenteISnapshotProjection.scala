package readside.proyectionists.registrales.componente_i.projectionists

import consumers.registral.componente_i.application.entities.{ComponenteITri, DetallesComponenteI}
import consumers.registral.componente_i.domain.ComponenteIEvents
import consumers.registral.componente_i.infrastructure.json.json._
import io.circe.parser._
import io.circe.syntax.EncoderOps

final case class ComponenteISnapshotProjection(
    event: ComponenteIEvents.ComponenteIPersistedSnapshot
) extends ComponenteIProjection {

  val registro: Option[ComponenteITri] = event.registro
  val bobDetailsResult: Option[Map[String, List[DetallesComponenteI]]] = {
    decode[Map[String, List[DetallesComponenteI]]](registro.get.BCI_OTROS_ATRIBUTOS.asJson.toString()).toOption
  }
  val mao: Map[String, String] = Map("BCI_DETALLES" -> bobDetailsResult.get("BCI_DETALLES").asJson.noSpaces)

  val fromRegistro: Option[List[(String, Object)]] = registro map { registro =>
    List(
      "bci_suj_identificador" -> event.sujetoId,
      "bci_soj_tipo_objeto" -> event.tipoObjeto,
      "bci_soj_identificador" -> event.objetoId,
      "bci_canal_origen" -> registro.BCI_CANAL_ORIGEN,
      "bci_otros_atributos" -> Some(mao),
      "bci_soj_identificador_2" -> registro.BCI_SOJ_IDENTIFICADOR_2,
    )
  }

  val bindings: List[(String, Object)] = fromRegistro.get
}
