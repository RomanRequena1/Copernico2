package consumers.registral.juicio.domain

import consumers.registral.juicio.application.entities.JuicioExternalDto.DetallesJuicio
import consumers.registral.juicio.application.entities.{JuicioExternalDto, JuicioMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class JuicioState(
    registro: Option[JuicioExternalDto] = None,
    detallesJuicio: Seq[DetallesJuicio] = Seq.empty,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[JuicioMessage, JuicioEvents, JuicioState] with CbroSerialization
