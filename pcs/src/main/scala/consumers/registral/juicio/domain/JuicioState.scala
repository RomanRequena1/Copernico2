package consumers.registral.juicio.domain

import java.time.LocalDateTime
import consumers.registral.juicio.application.entities.{DetallesJuicio,JuicioTri, JuicioMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class JuicioState(
    registro: Option[JuicioTri] = None,
    detallesJuicio: Seq[DetallesJuicio] = Seq.empty,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[JuicioMessage, JuicioEvents, JuicioState]
