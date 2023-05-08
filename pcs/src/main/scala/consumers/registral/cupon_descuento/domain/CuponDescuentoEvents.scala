package consumers.registral.cupon_descuento.domain

import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoExternalDto, CuponDescuentoMessage}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento
import design_principles.actor_model.Event

sealed trait CuponDescuentoEvents extends Event with CuponDescuentoMessage {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def obligacionId: String
}


object CuponDescuentoEvents {

  case class CuponDescuentoPersistedSnapshot(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObjeto: String,
                                          obligacionId: String,
                                          registro: Option[CuponDescuentoExternalDto],
                                        ) extends CuponDescuentoEvents

  case class CuponDescuentoUpdatedFromDto(
                                       deliveryId: BigInt,
                                       sujetoId: String,
                                       objetoId: String,
                                       tipoObjeto: String,
                                       obligacionId: String,
                                       registro: CuponDescuentoExternalDto,
                                       detallesCuponDescuento: Seq[DetallesCuponDescuento],
                                     ) extends CuponDescuentoEvents

  case class CuponDescuentoRemoved(
                                deliveryId: BigInt,
                                sujetoId: String,
                                objetoId: String,
                                tipoObjeto: String,
                                obligacionId: String,
                                registro: CuponDescuentoExternalDto,
                              ) extends CuponDescuentoEvents
}