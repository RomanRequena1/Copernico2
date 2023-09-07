package consumers.registral.componente_i.domain

import consumers.registral.componente_i.application.entities.{ComponenteIExternalDto, ComponenteIMessage}
import consumers.registral.componente_i.application.entities.ComponenteIExternalDto.DetallesComponenteI
import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoExternalDto, CuponDescuentoMessage}
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento

import java.time.LocalDateTime
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class ComponenteIState(
                        registro: Option[ComponenteIExternalDto] = None,
                        detallesComponenteI: Seq[DetallesComponenteI] = Seq.empty,
                        fechaUltMod: LocalDateTime = LocalDateTime.MIN
                      ) extends AbstractStateWithCQRS[ComponenteIMessage, ComponenteIEvents, ComponenteIState]