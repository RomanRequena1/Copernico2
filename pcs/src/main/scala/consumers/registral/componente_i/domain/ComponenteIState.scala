package consumers.registral.componente_i.domain

import consumers.registral.componente_i.application.entities.{ComponenteIMessage, ComponenteITri, DetallesComponenteI}
import cqrs.base_actor.typed.AbstractStateWithCQRS

import java.time.LocalDateTime

case class ComponenteIState(
                        registro: Option[ComponenteITri] = None,
                        detallesComponenteI: Seq[DetallesComponenteI] = Seq.empty,
                        fechaUltMod: LocalDateTime = LocalDateTime.MIN
                      ) extends AbstractStateWithCQRS[ComponenteIMessage, ComponenteIEvents, ComponenteIState]