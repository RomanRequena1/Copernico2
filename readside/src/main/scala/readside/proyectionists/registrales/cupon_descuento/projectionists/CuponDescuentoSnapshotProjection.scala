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
    decode[Map[String, List[DetallesCuponDescuento]]](registro.get.BOB_OTROS_ATRIBUTOS.asJson.toString()).toOption
  }
  val mao: Map[String, String] = Map("BOB_DETALLES" -> bobDetailsResult.get("BOB_DETALLES").asJson.noSpaces)
  val fromRegistro: Option[List[(String, Option[Object])]] = registro map { registro =>
    List(
      "bcd_canal_origen" -> registro.BOB_CANAL_ORIGEN,
      "bcd_otros_atributos" -> Some(mao),
      "bcd_soj_identificador_2" -> registro.BOB_SOJ_IDENTIFICADOR_2,
    )
  }

  val bindings: List[(String, Object)] = fromRegistro.get
}
