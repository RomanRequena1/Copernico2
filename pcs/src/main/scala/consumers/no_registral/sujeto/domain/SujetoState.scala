package consumers.no_registral.sujeto.domain

import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import ddd.{AbstractState, eventCounterMax}
import serialization.CbroSerialization

import java.time.LocalDateTime

final case class SujetoState(
                              saldo: BigDecimal = 0,
                              saldoObjetos: Map[String, BigDecimal] = Map.empty,
                              saldoObligaciones: Map[String, BigDecimal] = Map.empty,
                              objetos: Set[(String, String)] = Set.empty,
                              fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                              registro: Option[SujetoExternalDto] = None,
                              lastDeliveryIdByEvents:  BigInt = 0,
                              eventCounter:Int = 0,
                              cuotas: List[Boolean] = List(false, false, false, false, false, false, false, false, false, false, false, false, false),
                              deuda30Sujeto: Boolean = true,
                              objVencidas: Map[String, (Boolean, String)] = Map.empty, //todo este ("idObjeto" -> (valor30%, "tipoObjeto(sale del alta objeto) "))
                              diffStates: Boolean = false,                             //todo este ("IBG456" -> (true, "2")
                              lastInternalDeliveryId:BigInt = 0
                            ) extends AbstractState[SujetoEvents] with CbroSerialization{
  def +(event: SujetoEvents): SujetoState = {
    eventCounter match {
      case n if (n > (eventCounterMax)) => changeState(event).copy(
        fechaUltMod = LocalDateTime.now,
        lastDeliveryIdByEvents = event.deliveryId,
        eventCounter = 0
      )
      case n => changeState(event).copy(
        fechaUltMod = LocalDateTime.now,
        lastDeliveryIdByEvents = event.deliveryId,
        eventCounter = n + 1
      )
    }
    /*changeState(event).copy(
      fechaUltMod = LocalDateTime.now,
      lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, event.deliveryId)),
      eventCounter = c
    )*/
  }

  private def validExitsObjVencidas(objetoId: String, clasificacionObjeto: String) = {

    objVencidas match {
      case x if x.contains(objetoId) && x(objetoId)._2.equals("") => x updated (objetoId, (true, clasificacionObjeto))
      case x if x.contains(objetoId) => x updated (objetoId, (true, x(objetoId)._2))
      case x => x + (objetoId -> ((true, clasificacionObjeto)))
    }

    //if (objVencidas.contains(objetoId)) objVencidas else objVencidas + (objetoId -> (true, clasificacionObjeto))
  }
  private def diffCurrentStateAndNewState(currentObjVnecidad: Map[String, (Boolean, String)], newObjVnecidad: Map[String, (Boolean, String)], deuda30Sujeto: Boolean) = {
    println("CUMBIA -> newObjVnecidad" + newObjVnecidad)
    val map = newObjVnecidad.filter(obj => obj._2._2.equals("2"))
    println("CUMBIA -> map" + map)
    if (map.values.forall(_._1)) {

      val newTupla = (true, deuda30Sujeto.equals(true))
      newTupla
    }
    else {
      val newTupla = (false, deuda30Sujeto.equals(false))
      newTupla
    }
  }
  private def validExitsObjVencidasTreinta(objetoId: String,clasificacionObjeto: String) = {
    objVencidas match {
      case x if x.contains(objetoId) && x(objetoId)._2.equals("") => x updated (objetoId, (false, clasificacionObjeto))
      case x if x.contains(objetoId) => x updated (objetoId, (false, x(objetoId)._2))
      case x => x + (objetoId -> ((false, clasificacionObjeto)))
    }

    //if (objVencidas.contains(objetoId)) objVencidas updated (objetoId , (false, objVencidas(objetoId)._2 )) else objVencidas + (objetoId -> (false, clasificacionObjeto))
  }
  private def changeState(event: SujetoEvents): SujetoState =
    event match {
      case SujetoEvents.SujetoUpdatedFromTri(_, _, registro) =>
        copy(
          registro = Some(registro)
        )
      case SujetoEvents.SujetoUpdatedFromAnt(_, _, registro) =>
        copy(
          registro = Some(registro)
        )
      case SujetoEvents.SujetoUpdatedFromObjeto(deliveryId, _, objetoId, tipoObjeto, saldoObjeto, _saldoObligaciones, clasificacionObjeto) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos + (objetoKey -> saldoObjeto)
        val _objVencidas = validExitsObjVencidas(objetoId, clasificacionObjeto)
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, deuda30Sujeto)

        copy(
          objetos = objetos + ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones + (objetoKey -> _saldoObligaciones),
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          deuda30Sujeto = diff._1,
          diffStates = diff._2
        )
      case SujetoEvents.SujetoUpdatedFromObjetoTreintaPorciento(deliveryId, _, objetoId, tipoObjeto, saldoObjeto, _saldoObligaciones, clasificacionObjeto) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos + (objetoKey -> saldoObjeto)
        val _objVencidas = validExitsObjVencidasTreinta(objetoId, clasificacionObjeto)
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, deuda30Sujeto)

        copy(
          objetos = objetos + ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones + (objetoKey -> _saldoObligaciones),
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          deuda30Sujeto = diff._1,
          diffStates = diff._2
        )
      case SujetoEvents.SujetoBajaFromObjetoSet(deliveryId, _, objetoId, tipoObjeto) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos - objetoKey
        val _objVencidas = objVencidas - objetoId
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, deuda30Sujeto)
        copy(
          objetos = objetos - ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones - objetoKey,
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          diffStates = diff._2,
          deuda30Sujeto = diff._1
        )
      case evt: SujetoEvents.SujetoSnapshotPersisted =>
        copy(
          saldo = evt.saldo
        )
      case _ => this
    }
}
