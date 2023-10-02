package consumers.registral.parametrica_plan.domain

import java.time.LocalDateTime
import consumers.registral.parametrica_plan.application.entities.{ ParametricaPlanMessage, ParametricaPlanTri}
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class ParametricaPlanState(
                                 registro: Option[ParametricaPlanTri] = None,
                                 fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[ParametricaPlanMessage, ParametricaPlanEvents, ParametricaPlanState]
