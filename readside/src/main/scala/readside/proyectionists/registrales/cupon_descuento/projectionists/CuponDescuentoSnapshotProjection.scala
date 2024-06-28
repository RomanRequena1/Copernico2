package readside.proyectionists.registrales.cupon_descuento.projectionists

import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoTri, DetallesCuponDescuento}
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents
import consumers.registral.cupon_descuento.infrastructure.json.json._
import io.circe.parser._
import io.circe.syntax.EncoderOps
final case class CuponDescuentoSnapshotProjection(
                                                   event: CuponDescuentoEvents.CuponDescuentoPersistedSnapshot
                                                 ) extends CuponDescuentoProjection {

  val registro: Option[CuponDescuentoTri] = event.registro
  val bobDetailsResult: Option[Map[String, List[DetallesCuponDescuento]]] = {

    decode[Map[String, List[DetallesCuponDescuento]]](registro.get.BCD_OTROS_ATRIBUTOS.asJson.toString()).toOption
  }
  val mao: Map[String, String] = Map("BCD_DETALLES" -> bobDetailsResult.get("BCD_DETALLES").asJson.noSpaces)
  val fromRegistro: Option[List[(String, Object)]] = registro map { registro =>
    List(
      "bcd_suj_identificador" -> event.sujetoId,
      "bcd_soj_tipo_objeto" -> event.tipoObjeto,
      "bcd_soj_identificador" -> event.objetoId,
      "bcd_canal_origen" -> registro.BCD_CANAL_ORIGEN,
      "bcd_otros_atributos" -> Some(mao),
      "bcd_soj_identificador_2" -> registro.BCD_SOJ_IDENTIFICADOR_2,
    )
  }

  val bindings: List[(String, Object)] = fromRegistro.get
}
