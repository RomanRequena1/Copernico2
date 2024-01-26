package consumers.no_registral.tranferencia.domain

import ddd.AbstractState
import serialization.CbroSerialization

import java.time.LocalDateTime


final case class TransferenciaState(
    objetoId: String = "",
    tipoObj: String = "",
    sujetoIdActual: Option[VinculoActual] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    eventCounter: Int = 0,
    map: Map[VinculoActual, Boolean] = Map.empty,
    tiene30ObjetoTranf: Boolean = false
                                   ) extends AbstractState[TransferenciaEvent] with CbroSerialization{


  def +(event: TransferenciaEvent): TransferenciaState  = {
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
  private def changeState(event: TransferenciaEvent): TransferenciaState =
    event match {
      case evt: TransferenciaEvent.CreatedVinculoObjSujToTransf =>
        copy(
          tiene30ObjetoTranf = false
        )
      case evt =>
        log.warn(s"Unexpected event at TransferenciaState ${evt}")
        this
    }
}



