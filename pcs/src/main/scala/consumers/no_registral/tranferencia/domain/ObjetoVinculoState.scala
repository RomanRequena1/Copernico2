package consumers.no_registral.tranferencia.domain

import ddd.AbstractState
import serialization.CbroSerialization

import java.time.LocalDateTime


final case class ObjetoVinculoState(
    objetoId: String = "",
    tipoObj: String = "",
    sujetoIdActual: Option[VinculoActual] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    eventCounter: Int = 0,
    map: Map[VinculoActual, Boolean] = Map.empty,
    tiene30ObjetoTranf: Boolean = false
                                   ) extends AbstractState[ObjetoVinculoEvent] with CbroSerialization{


  def +(event: ObjetoVinculoEvent): ObjetoVinculoState  = {
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
  private def changeState(event: ObjetoVinculoEvent): ObjetoVinculoState =
    event match {
      case evt: ObjetoVinculoEvent.CreatedObjetoVinculoFromObj =>
        copy(
          tiene30ObjetoTranf = false,
          map = map + (VinculoActual(evt.sujetoId,evt.objetoId,evt.tipoObj) -> evt.tiene30ObjetoTranf)
        )
      case _ =>
        log.warn(s"Unexpected event at ObjetoVinculoState ")
        this
    }
}



