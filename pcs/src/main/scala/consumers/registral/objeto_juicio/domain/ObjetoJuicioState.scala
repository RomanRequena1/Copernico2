package consumers.registral.objeto_juicio.domain

import consumers.registral.objeto_juicio.application.entities.{ObjetoJuicioExternalDto, ObjetoJuicioMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization
import java.time.LocalDateTime

final case class ObjetoJuicioState(
                                    registro: Option[ObjetoJuicioExternalDto] = None,
                                    lastDeliveryIdByEvent: BigInt = 0,
                                    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                                    eventCounter:Int = 0,
                                  ) extends AbstractStateWithCQRS[ObjetoJuicioMessage, ObjetoJuicioEvents, ObjetoJuicioState] with CbroSerialization