package consumers.registral.etapas_procesales.domain

import consumers.registral.etapas_procesales.application.entities.{EtapasProcesalesExternalDto, EtapasProcesalesMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class EtapasProcesalesState(
                                  registro: Option[EtapasProcesalesExternalDto] = None,
                                  fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[EtapasProcesalesMessage, EtapasProcesalesEvents, EtapasProcesalesState] with CbroSerialization
