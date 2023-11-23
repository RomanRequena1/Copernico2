package consumers.registral.exclusiones_sujeto.domain

import consumers.registral.exclusiones_sujeto.application.entities.{ExclusionesSujetoExternalDto, ExclusionesSujetoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class ExclusionesSujetoState(
                                  registro: Option[ExclusionesSujetoExternalDto] = None,
                                  fechaUltMod: LocalDateTime = LocalDateTime.MIN
                                ) extends AbstractStateWithCQRS[ExclusionesSujetoMessage, ExclusionesSujetoEvents, ExclusionesSujetoState] with CbroSerialization
