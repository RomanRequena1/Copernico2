package consumers.no_registral.tranferencia.domain

import ddd.AbstractState
import serialization.CbroSerialization

import java.time.LocalDateTime


final case class ObjetoVinculoState(
    objetoId: String = "",
    tipoObj: String = "",
    sujetoIdActual: Option[Vinculo] = None, //todo vinculo actual cuando el objeto tiene transferencia, si no hay esta en None
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    eventCounter: Int = 0,
    mapTransf: Map[Vinculo, VinculoCotitular] = Map.empty, //todo contiene todos los vinculos que son transf junto con el tiene30Objeto
    mapVinculo: Map[Vinculo, VinculoCotitular] = Map.empty, //todo contiene todos los vinculos que no son transf junto con el tiene30Objeto
    tiene30ObjetoVinculo: Boolean = false //todo si ese objeto tiene 30 que depende de todos los vinculos, depende el caso
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

  private def calcular30desdeMapVinculo(mapVinculo: Map[Vinculo, VinculoCotitular]): Boolean =
    if (mapVinculo.forall(_._2.tiene30Objeto)) true else false //todo sacar si no se hace mas compleja despues
  private def validExitsObjVinculo(vinculo: Vinculo, vinculoCotitular: VinculoCotitular) = {

    mapVinculo match {
      //case x if x.contains(objetoId) && x(objetoId)._2.equals("") => x updated (objetoId, (true, clasificacionObjeto))
      case x if x.contains(vinculo) => x updated(vinculo, vinculoCotitular)
      case x => x + (vinculo -> (vinculoCotitular))
    }
  }
  private def changeState(event: ObjetoVinculoEvent): ObjetoVinculoState =
    event match {
      case evt: ObjetoVinculoEvent.UpdatedVinculoObjetoFromObj =>
        val _vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val _vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.estadoObj, evt.titularidad)
        val _Map = mapVinculo + (_vinculo -> _vinculoCotitular)
        println("CUMBIA " + calcular30desdeMapVinculo(_Map))
        val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(_Map)

        copy(
          tiene30ObjetoVinculo = _tiene30ObjetoVinculo,
          mapVinculo = _Map
        )
      case _ =>
        log.warn(s"Unexpected event at ObjetoVinculoState ")
        this
    }
}



