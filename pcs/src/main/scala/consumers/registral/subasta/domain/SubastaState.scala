package consumers.registral.subasta.domain

import java.time.LocalDateTime
import consumers.registral.subasta.application.entities.{SubastaExternalDto, SubastaMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

case class SubastaState(
    registro: Option[SubastaExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[SubastaMessage, SubastaEvents, SubastaState] with CbroSerialization
