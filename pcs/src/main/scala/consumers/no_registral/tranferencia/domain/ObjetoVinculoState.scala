package consumers.no_registral.tranferencia.domain

import ddd.AbstractState
import serialization.CbroSerialization

import java.time.LocalDateTime

final case class ObjetoVinculoState(
                                     objetoId: String = "",
                                     tipoObj: String = "",
                                     sujetoIdActual: Option[Vinculo] = None,
                                     fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                                     eventCounter: Int = 0,
                                     mapTransf: Map[Vinculo, VinculoCotitular] = Map.empty,
                                     mapVinculo: Map[Vinculo, VinculoCotitular] = Map.empty,
                                     tiene30ObjetoVinculo: Boolean = true,
                                     exclusionObjetoVinculo: Option[String] = None,
                                     lastDeliveryIdByEvents: BigInt = 0,
                                     ultimoAplicarDescuentoEnviado: Option[Boolean] = None,
                                     fechaUltimoEnvioResumen: LocalDateTime = LocalDateTime.MIN,
                                     dmnNumeroVinculo: Option[Int] = None,
                                     dmnDescripcionVinculo: Option[String] = None,
                                     idExterno: Option[String] = None
                                   ) extends AbstractState[ObjetoVinculoEvent]
  with CbroSerialization {

  def +(event: ObjetoVinculoEvent): ObjetoVinculoState = {
    eventCounter match {
      case n if (n > (50)) =>
        changeState(event).copy(
          fechaUltMod = LocalDateTime.now,
          eventCounter = 0,
          lastDeliveryIdByEvents = event.deliveryId
        )
      case n =>
        changeState(event).copy(
          fechaUltMod = LocalDateTime.now,
          eventCounter = n + 1,
          lastDeliveryIdByEvents = event.deliveryId
        )
    }
  }

  /**
   * 1. Si el mapTransf esta vacio, se verifica que todos los vinculos del mapVinculo tengan 30
   * 2. Si el mapTransf no esta vacio, se verifica que todos los vinculos del mapVinculo y del mapTransf tengan 30
   */
  private def calcular30desdeMapVinculo(
                                         _mapVinculo: Map[Vinculo, VinculoCotitular],
                                         _mapTransf: Map[Vinculo, VinculoCotitular]
                                       ): Boolean = {
    if (_mapTransf.isEmpty) {
      if (_mapVinculo.forall(_._2.tiene30Objeto)) true else false
    } else {
      if (_mapVinculo.forall(_._2.tiene30Objeto) && _mapTransf.forall(_._2.tiene30Objeto)) true else false
    }
  }

  /**
   * 1. Si el vinculo ya existe en el map, se actualiza el valor del vinculo
   * 2. Si el vinculo no existe en el map, se agrega el vinculo al map
   */
  private def UpdateObjVinculo(_vinculo: Vinculo, _vinculoCotitular: VinculoCotitular) = {
    mapVinculo match {
      case x if x.contains(_vinculo) =>
        x updated (_vinculo, _vinculoCotitular)
      case x =>
        x + (_vinculo -> _vinculoCotitular)
    }
  }

  /**
   * Si mapTransf no contiene el vinculo, se agrega el vinculo al map solo si el vinculo es responsable y tiene 30 es false
   * Si mapTransf contiene el vinculo, se verifica si el valor del vinculo es igual al valor del vinculoCotitular,
   * si es igual no se hace nada, si es distinto se elimina el vinculo del map
   */
  private def updateMapTransf(
                               _vinculo: Vinculo,
                               _vinculoCotitular: VinculoCotitular
                             ): Map[Vinculo, VinculoCotitular] = {
    if (mapTransf.filterNot(x => !x._1.sujetoId.equals(_vinculo.sujetoId)).isEmpty) {
      if (_vinculoCotitular.isResponsable.get && _vinculoCotitular.tiene30Objeto.equals(false)) {
        mapTransf + (_vinculo -> _vinculoCotitular)
      } else {
        mapTransf
      }
    } else {
      val _vinculoOld = mapTransf.find(e => e._1.equals(_vinculo))
      if (_vinculoOld.isDefined) {
        if (_vinculoOld.get._2.tiene30Objeto.equals(_vinculoCotitular.tiene30Objeto))
          mapTransf
        else
          mapTransf - _vinculo
      } else {
        mapTransf
      }
    }
  }

  /**
   * CORRECCION: Actualiza el DMN siempre que venga un valor válido en el evento.
   * Esto evita que quede "congelado" un valor inicial incorrecto.
   *
   * @param evtDmnNumero DMN número del evento
   * @param evtDmnDescripcion DMN descripción del evento
   * @param objetoIdLog ID del objeto para logging
   * @return Tupla con (dmnNumero, dmnDescripcion) a usar
   */
  private def actualizarDmn(
                             evtDmnNumero: Option[Int],
                             evtDmnDescripcion: Option[String],
                             objetoIdLog: String
                           ): (Option[Int], Option[String]) = {
    (evtDmnNumero, evtDmnDescripcion) match {
      case (Some(num), Some(desc)) =>
        // CORRECCION: Siempre actualizar con el valor más reciente del evento
        // Esto garantiza consistencia entre tiene30Objeto y la descripción del DMN
        if (dmnNumeroVinculo.isDefined && (dmnNumeroVinculo != Some(num) || dmnDescripcionVinculo != Some(desc))) {
          log.info(
            s"[DMN-UPDATE] objetoId=$objetoIdLog - Actualizando DMN de " +
              s"(numero=${dmnNumeroVinculo.getOrElse("None")}, desc=${dmnDescripcionVinculo.getOrElse("None")}) a " +
              s"(numero=$num, desc=$desc)"
          )
        }
        (Some(num), Some(desc))
      case (None, None) =>
        // Si el evento no trae DMN, mantener el existente
        (dmnNumeroVinculo, dmnDescripcionVinculo)
      case _ =>
        // Caso parcial (solo uno de los dos): mantener el existente para evitar inconsistencias
        log.warn(
          s"[DMN-PARTIAL] objetoId=$objetoIdLog - Evento con DMN parcial ignorado: " +
            s"numero=$evtDmnNumero, desc=$evtDmnDescripcion"
        )
        (dmnNumeroVinculo, dmnDescripcionVinculo)
    }
  }

  private def changeState(event: ObjetoVinculoEvent): ObjetoVinculoState =
    event match {
      case evt: ObjetoVinculoEvent.UpdatedVinculoObjetoFromObj =>
        val _vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val _vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.titularidad, evt.estadoObj)

        val estaEnVinculo = mapVinculo.contains(_vinculo)
        val estaEnTransf = mapTransf.contains(_vinculo)

        val _mapVinculo = if (estaEnVinculo) {
          UpdateObjVinculo(_vinculo, _vinculoCotitular)
        } else if (!estaEnTransf) {
          mapVinculo + (_vinculo -> _vinculoCotitular)
        } else {
          mapVinculo
        }

        val _mapTransf = if (estaEnTransf) {
          mapTransf.updated(_vinculo, _vinculoCotitular)
        } else {
          mapTransf
        }

        val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(_mapVinculo, _mapTransf)

        // CORRECCION: Usar el método centralizado para actualizar DMN
        val (_dmnNumero, _dmnDescripcion) = actualizarDmn(evt.dmnNumero, evt.dmnDescripcion, evt.objetoId)

        copy(
          lastDeliveryIdByEvents = evt.deliveryId,
          tiene30ObjetoVinculo = _tiene30ObjetoVinculo,
          mapVinculo = _mapVinculo,
          mapTransf = _mapTransf,
          exclusionObjetoVinculo = evt.exclusionObjeto,
          dmnNumeroVinculo = _dmnNumero,
          dmnDescripcionVinculo = _dmnDescripcion,
          idExterno = evt.idExterno
        )

      case evt: ObjetoVinculoEvent.CreatedTransfVinculoObjetoFromObj =>
        val _vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val _vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.titularidad, evt.estadoObj)

        val vinculosAnteriores = mapVinculo.filter(v => v._1.objetoId.equals(evt.objetoId))
        val (nuevoMapTransf, nuevoMapVinculo) = evt.estadoObj match {
          case Some("TRANSF") =>
            val _mapTransf = vinculosAnteriores.foldLeft(mapTransf) {
              case (acc, (vinculo, cotitular)) =>
                acc + (vinculo -> cotitular.copy(estado = Some("TRANSF")))
            }
            val _mapVinculoLimpio = mapVinculo.filterNot(v => v._1.objetoId.equals(evt.objetoId))
            val _mapVinculo = _mapVinculoLimpio + (_vinculo -> _vinculoCotitular)
            (_mapTransf, _mapVinculo)

          case Some("ESTADO2") =>
            val _mapTransf = vinculosAnteriores.foldLeft(mapTransf) {
              case (acc, (vinculo, cotitular)) =>
                acc + (vinculo -> cotitular.copy(estado = Some("ESTADO2")))
            }
            val _mapVinculoLimpio = mapVinculo.filterNot(v => v._1.objetoId.equals(evt.objetoId))
            val _mapVinculo = _mapVinculoLimpio + (_vinculo -> _vinculoCotitular)
            (_mapTransf, _mapVinculo)

          case _ =>
            val _mapVinculo = mapVinculo + (_vinculo -> _vinculoCotitular)
            (mapTransf, _mapVinculo)
        }

        // CORRECCION: Usar el método centralizado para actualizar DMN
        val (_dmnNumero, _dmnDescripcion) = actualizarDmn(evt.dmnNumero, evt.dmnDescripcion, evt.objetoId)

        val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(nuevoMapVinculo, nuevoMapTransf)

        copy(
          tiene30ObjetoVinculo = _tiene30ObjetoVinculo,
          mapVinculo = nuevoMapVinculo,
          mapTransf = nuevoMapTransf,
          idExterno = evt.idExterno,
          exclusionObjetoVinculo = evt.exclusionObjeto,
          dmnNumeroVinculo = _dmnNumero,
          dmnDescripcionVinculo = _dmnDescripcion
        )

      case evt: ObjetoVinculoEvent.RemovedVinculoObjetoFromObj =>
        val _vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val _mapVinculo = if (mapVinculo.contains(_vinculo)) mapVinculo - _vinculo else mapVinculo
        val _mapTransf = if (mapTransf.contains(_vinculo)) mapTransf - _vinculo else mapTransf
        val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(_mapVinculo, _mapTransf)
        copy(
          tiene30ObjetoVinculo = _tiene30ObjetoVinculo,
          mapVinculo = _mapVinculo,
          mapTransf = _mapTransf
        )

      case evt: ObjetoVinculoEvent.ResumenEnviado =>
        copy(
          ultimoAplicarDescuentoEnviado = evt.aplicarDescuento,
          idExterno = evt.idExterno,
          fechaUltimoEnvioResumen = evt.fechaEnvio
        )

      case _ =>
        log.warn(s"Unexpected event at ObjetoVinculoState ")
        this
    }
}