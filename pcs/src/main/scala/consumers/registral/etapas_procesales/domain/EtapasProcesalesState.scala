package consumers.registral.etapas_procesales.domain

import java.time.LocalDateTime
import consumers.registral.etapas_procesales.application.entities.{ EtapasProcesalesMessage, EtapasProcesalesTri}
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class EtapasProcesalesState(
                                  registro: Option[EtapasProcesalesTri] = None,
                                  fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[EtapasProcesalesMessage, EtapasProcesalesEvents, EtapasProcesalesState]
