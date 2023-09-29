
package consumers.registral.cupon_descuento.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait CuponDescuentoResponses extends CbroSerialization


object CuponDescuentoResponses {

  case class GetCuponDescuentoResponse(registro: Option[CuponDescuentoTri] = None,
                               detallesCuponDescuento: Seq[DetallesCuponDescuento],
                               fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response with CbroSerialization

}