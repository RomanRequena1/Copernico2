package consumers.registral.parametrica_recargo.domain

import consumers.registral.parametrica_recargo.application.entities.{ParametricaRecargoExternalDto, ParametricaRecargoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS

import java.time.LocalDateTime

case class ParametricaRecargoState(
                                    registro: Option[ParametricaRecargoExternalDto] = None,
                                    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[ParametricaRecargoMessage, ParametricaRecargoEvents, ParametricaRecargoState]
