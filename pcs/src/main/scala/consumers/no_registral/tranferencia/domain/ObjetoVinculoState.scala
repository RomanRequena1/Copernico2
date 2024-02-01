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

  private def calcular30desdeMapVinculo(_mapVinculo: Map[Vinculo, VinculoCotitular], _mapTransf: Map[Vinculo, VinculoCotitular]): Boolean = {
    println("CUMBIA calcular30desdeMapVinculo " + _mapVinculo + " - " + _mapTransf)
    if(_mapTransf.isEmpty) {
        val __mapVinculo = _mapVinculo.filter(e => !e._2.estado.getOrElse("").equals("TRANSF"))
        if (__mapVinculo.forall(_._2.tiene30Objeto)) true else false //todo sacar si no se hace mas compleja despues}
    } else {
      val __mapVinculo = _mapVinculo.filter(e => !e._2.estado.getOrElse("").equals("TRANSF"))
      if (__mapVinculo.forall(_._2.tiene30Objeto) && _mapTransf.forall(_._2.tiene30Objeto)) true else false
    }
  }

// fuction that update the mapVinculo with the new vinculo and return the new map, but if the vinculo is already in the map, it update the vinculo
// def UpdateObjVinculo(vinculo: Vinculo, vinculoCotitular: VinculoCotitular): Map[Vinculo, VinculoCotitular] = {
//   if (mapVinculo.contains(vinculo))
//     mapVinculo.updated(vinculo, vinculoCotitular)
//   else
//     mapVinculo + (vinculo -> vinculoCotitular)
//
// }


  private def UpdateObjVinculo(vinculo: Vinculo, vinculoCotitular: VinculoCotitular) = {
    println("CUMBIA UpdateObjVinculo " + vinculoCotitular  + " - " + vinculo)
    mapVinculo match {
      //case x if x.contains(objetoId) && x(objetoId)._2.equals("") => x updated (objetoId, (true, clasificacionObjeto))
      case x if x.contains(vinculo) => x updated(vinculo, vinculoCotitular)
      case x => x + (vinculo -> vinculoCotitular)
    }
  }


  private def updateMapTransf(_vinculo: Vinculo, _vinculoCotitular: VinculoCotitular): Map[Vinculo, VinculoCotitular] = {
    if(!mapTransf.contains(_vinculo)){
        if(_vinculoCotitular.isResponsable.get && _vinculoCotitular.tiene30Objeto.equals(false)) mapTransf + (_vinculo -> _vinculoCotitular) else mapTransf
    }
    else {
        val _vinculoOld = mapTransf.find(e => e._1.equals(_vinculo))
        if(_vinculoOld.isDefined) {
          if(_vinculoOld.get._2.tiene30Objeto.equals(_vinculoCotitular.tiene30Objeto))
            mapTransf
          else
            mapTransf - _vinculo
        }
        else {
          mapTransf
        }
    }
  }




  private def changeState(event: ObjetoVinculoEvent): ObjetoVinculoState =
    event match {
      case evt: ObjetoVinculoEvent.UpdatedVinculoObjetoFromObj =>
        val _vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val _vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.titularidad, evt.estadoObj)
        val _mapVinculo = UpdateObjVinculo(_vinculo, _vinculoCotitular)
        println("CUMBIA " + calcular30desdeMapVinculo(_mapVinculo, mapTransf))
        val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(_mapVinculo, mapTransf)

        copy(
          tiene30ObjetoVinculo = _tiene30ObjetoVinculo,
          mapVinculo = _mapVinculo
        )
      case evt: ObjetoVinculoEvent.CreatedTransfVinculoObjetoFromObj =>
        val _vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val _vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.titularidad, evt.estadoObj)
        val _mapVinculo = if (_vinculoCotitular.isResponsable.get) mapVinculo - _vinculo else UpdateObjVinculo(_vinculo, _vinculoCotitular)
        val _mapTransf = updateMapTransf(_vinculo, _vinculoCotitular)
        val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(_mapVinculo, _mapTransf)
        copy(
          tiene30ObjetoVinculo = _tiene30ObjetoVinculo,
          mapVinculo = _mapVinculo,
          mapTransf = _mapTransf
        )
      case _ =>
        log.warn(s"Unexpected event at ObjetoVinculoState ")
        this
    }
}



