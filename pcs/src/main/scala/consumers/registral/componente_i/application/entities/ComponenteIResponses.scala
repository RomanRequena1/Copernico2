
package consumers.registral.componente_i.application.entities

import consumers.registral.componente_i.application.entities.ComponenteIExternalDto.DetallesComponenteI
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento

import java.time.LocalDateTime

sealed trait ComponenteIResponses


object ComponenteIResponses {

  case class GetComponenteIResponse(registro: Option[ComponenteIExternalDto] = None,
                               detallesComponenteI: Seq[DetallesComponenteI],
                               fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response

}