package consumers.registral.juicio_obn.domain

import consumers.registral.juicio_obn.application.entities.{JuicioObnMessage, JuicioObnTri}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime


case class JuicioObnState(
                         registro: Option[JuicioObnTri] = None,
                         lastDeliveryIdByEvent: BigInt = 0,
                         fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                         eventCounter:Int = 0,
                         ) extends AbstractStateWithCQRS[JuicioObnMessage, JuicioObnEvents, JuicioObnState] with CbroSerialization
