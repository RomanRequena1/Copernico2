package consumers.registral.parametrica_plan.domain

import consumers.registral.parametrica_plan.application.entities.{ParametricaPlanExternalDto, ParametricaPlanMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class ParametricaPlanState(
                                 registro: Option[ParametricaPlanExternalDto] = None,
                                 fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[ParametricaPlanMessage, ParametricaPlanEvents, ParametricaPlanState] with CbroSerialization
