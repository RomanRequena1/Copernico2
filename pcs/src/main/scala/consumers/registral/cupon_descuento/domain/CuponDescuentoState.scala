package consumers.registral.cupon_descuento.domain


import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoMessage, CuponDescuentoTri, DetallesCuponDescuento}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class CuponDescuentoState(
                        registro: Option[CuponDescuentoTri] = None,
                        detallesCuponDescuento: Seq[DetallesCuponDescuento] = Seq.empty,
                        fechaUltMod: LocalDateTime = LocalDateTime.MIN
                      ) extends AbstractStateWithCQRS[CuponDescuentoMessage, CuponDescuentoEvents, CuponDescuentoState] with CbroSerialization