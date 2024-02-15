package consumers.no_registral.exclusiones_objeto.domain

import consumers.no_registral.exclusiones_objeto.application.entities.{ExclusionesObjetoExternalDto, ExclusionesObjetoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import ddd.AbstractState
import serialization.CbroSerialization

import java.time.LocalDateTime

final case class ExclusionesObjetoState(
                                         objetoId: String = "",
                                         eventCounter: Int = 0,
                                         lastDeliveryIdByEvents:  BigInt = 0,
                                         fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                                         registro: Option[ExclusionesObjetoExternalDto] = None

                                       ) extends AbstractState[ExclusionesObjetoEvents] with CbroSerialization{



  def +(event: ExclusionesObjetoEvents): ExclusionesObjetoState  = {
    eventCounter match {
      case n if (n > (50)) => changeState(event).copy(
        fechaUltMod = LocalDateTime.now,
        eventCounter = 0
      )
      case n => changeState(event).copy(
        fechaUltMod = LocalDateTime.now,
        eventCounter = n + 1
      )
    }
    /*changeState(event).copy(
      fechaUltMod = LocalDateTime.now,
      lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, event.deliveryId)),
      eventCounter = eventCounter + 1
    )*/
  }

  private def changeState(event: ExclusionesObjetoEvents): ExclusionesObjetoState =
    event match {
      case ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto =>

    }

  def empty = ExclusionesObjetoEvents()

}
