package consumers.registral.exclusiones_objeto.domain

import consumers.registral.exclusiones_objeto.application.entities.{ExclusionesObjetoExternalDto, ExclusionesObjetoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class ExclusionesObjetoState(
                                  registro: Option[ExclusionesObjetoExternalDto] = None,
                                  fechaUltMod: LocalDateTime = LocalDateTime.MIN
                                ) extends AbstractStateWithCQRS[ExclusionesObjetoMessage, ExclusionesObjetoEvents, ExclusionesObjetoState] with CbroSerialization
