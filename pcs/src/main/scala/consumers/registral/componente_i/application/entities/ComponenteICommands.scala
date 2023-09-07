package consumers.registral.componente_i.application.entities

import consumers.registral.componente_i.application.entities.ComponenteIExternalDto.DetallesComponenteI
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento
import design_principles.actor_model.Command

sealed trait ComponenteICommands extends Command with ComponenteIMessage


object ComponenteICommands {
  case class ComponenteIUpdateFromDto(
                                      deliveryId: BigInt,
                                      sujetoId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      obligacionId: String,
                                      registro: ComponenteIExternalDto,
                                      detallesComponenteI: Seq[DetallesComponenteI]
                                    ) extends ComponenteICommands

  case class ComponenteIRemove(
                               deliveryId: BigInt,
                               sujetoId: String,
                               objetoId: String,
                               tipoObjeto: String,
                               obligacionId: String,
                               registro: ComponenteIExternalDto,
                               detallesComponenteI: Seq[DetallesComponenteI]
                             ) extends ComponenteICommands
}


