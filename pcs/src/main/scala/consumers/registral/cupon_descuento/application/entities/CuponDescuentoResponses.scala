
package consumers.registral.cupon_descuento.application.entities

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento


import java.time.LocalDateTime

sealed trait CuponDescuentoResponses


object CuponDescuentoResponses {

  case class GetCuponDescuentoResponse(registro: Option[CuponDescuentoExternalDto] = None,
                               detallesCuponDescuento: Seq[DetallesCuponDescuento],
                               fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response

}