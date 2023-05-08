package consumers.registral.cupon_descuento.domain


import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoExternalDto, CuponDescuentoMessage}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento

import java.time.LocalDateTime
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class CuponDescuentoState(
                        registro: Option[CuponDescuentoExternalDto] = None,
                        detallesCuponDescuento: Seq[DetallesCuponDescuento] = Seq.empty,
                        fechaUltMod: LocalDateTime = LocalDateTime.MIN
                      ) extends AbstractStateWithCQRS[CuponDescuentoMessage, CuponDescuentoEvents, CuponDescuentoState]