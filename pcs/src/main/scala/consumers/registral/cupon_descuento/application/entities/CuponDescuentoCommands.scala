package consumers.registral.cupon_descuento.application.entities

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento
import design_principles.actor_model.Command

sealed trait CuponDescuentoCommands extends Command with CuponDescuentoMessage


object CuponDescuentoCommands {
  case class CuponDescuentoUpdateFromDto(
                                      deliveryId: BigInt,
                                      sujetoId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      obligacionId: String,
                                      registro: CuponDescuentoExternalDto,
                                      detallesCuponDescuento: Seq[DetallesCuponDescuento]
                                    ) extends CuponDescuentoCommands

  case class CuponDescuentoRemove(
                               deliveryId: BigInt,
                               sujetoId: String,
                               objetoId: String,
                               tipoObjeto: String,
                               obligacionId: String,
                               registro: CuponDescuentoExternalDto,
                               detallesCuponDescuento: Seq[DetallesCuponDescuento]
                             ) extends CuponDescuentoCommands
}


