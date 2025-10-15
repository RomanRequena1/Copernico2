package consumers.no_registral.objeto.domain

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.objeto.domain.ObjetoEvents.AplicarDescuentoUpdated
import ddd.{AbstractState, eventCounterMax}
import serialization.CbroSerialization

import java.time.LocalDateTime

case class ObjetoState(
    saldo: BigDecimal = 0,
    obligacionesSaldo: Map[String, BigDecimal] = Map.empty,
    obligaciones: Set[String] = Set.empty,
    sujetos: Set[String] = Set.empty,
    sujetoResponsable: Option[String] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    registro: Option[ObjetoExternalDto] = None,
    tags: Set[String] = Set.empty,
    isResponsable: Boolean = false,
    lastDeliveryIdByEvents: BigInt = 0,
    porcentajeResponsabilidad: BigDecimal = 0,
    exenciones: Set[Exencion] = Set.empty,
    isBaja: Boolean = false,
    isAdheridoDebito: Boolean = false,
    eventCounter: Int = 0,
    cuotas: List[Boolean] =
      List(false, false, false, false, false, false, false, false, false, false, false, false, false),
    tiene30Objeto: Boolean = true,
    tiene30ObjetoVinculo: Boolean = true,
    ultimo30Objeto: Map[String, Boolean] = Map.empty,  //aca vemos cuando cambia el tiene30
    clasificacionObjeto: String = "2",
    tiene30Sujeto: Option[Boolean] = None,
    aplicarDescuento: Option[Boolean] = None,
    resulDmn: Option[Int] = None,
    obnVencidas: Map[String, Boolean] = Map.empty,
    deuda30Objeto: Boolean = true,
    tipoExclusion: String = "",
    exclusionObjeto: Option[String] = None,
    exclusionObjetoVinculo: Option[String] = None,
    dmnNumero: Option[Int] = None,
    dmnDescripcion : Option[String] = None,
    dmnDescripcionAnterior: Option[String] = None,
    dmnDescripcionAnteriorPorSujeto: Option[String] = None,
    exclusionSujeto: Option[String] = None,
                      ) extends AbstractState[ObjetoEvents]
    with CbroSerialization {

  override def +(event: ObjetoEvents): ObjetoState = {
    eventCounter match {
      case n if (n > (eventCounterMax)) =>
        changeState(event).copy(
          fechaUltMod = LocalDateTime.now,
          lastDeliveryIdByEvents = event.deliveryId,
          eventCounter = 0
        )
      case n =>
        changeState(event).copy(
          fechaUltMod = LocalDateTime.now,
          lastDeliveryIdByEvents = event.deliveryId,
          eventCounter = n + 1
        )
    }
    /*changeState(event).copy(
      fechaUltMod = LocalDateTime.now,
      lastDeliveryIdByEvents = lastDeliveryIdByEvents + ((event.getClass.getSimpleName, event.deliveryId)),
      eventCounter = eventCounter + 1
    )*/
  }

  /** En el array de ObnVencidas
   *
   * Las obligaciones en true son no deuda ( tiene30obligaciones = true )
   * Las Obligaciones en false son deuda ( tiene30obligaciones = false )
   *
   * @param obligacionId
   * @return
   */
  private def validExitsObnVencidas(obligacionId: String) = {
    obnVencidas match {
      case x if x.contains(obligacionId) => x updated (obligacionId, true) //la modifico si existe
      case x => x + (obligacionId -> true) //la creo sino existe
    }
  }

  /**
   * 1. Si todos los valores del map son true y tiene30ObjetoVinculo es true, entonces tiene30Objeto es true y sino es false
   */
  private def diffCurrentStateAndNewStateTest(currentObnVencidas: Map[String, Boolean],
                                              _tiene30ObjetoVinculo: Boolean) = { //todo cambiar nombre de funcion

    if (currentObnVencidas.values.forall(_ == true) && _tiene30ObjetoVinculo.equals(true)) {
      true
    } else {
      false
    }
  }

  private def diffCurrentStateAndNewState(currentObnVencidas: Map[String, Boolean], _tiene30ObjetoVinculo: Boolean) = { //todo cambiar nombre de funcion
    if (currentObnVencidas.values.forall(_ == true)) {
      true
    } else {
      false
    }
  }

  /** En el array de ObnVencidas
   *
   * Las obligaciones en true son no deuda ( tiene30obligaciones = true )
   *
   * Las Obligaciones en false son deuda ( tiene30obligaciones = false )
   *
   * Agrega la obligacion con deuda al array de ObnVencidas (Obn NoDeuda?)
   *
   * @param obligacionId
   * @return
   */
  private def validExitsObnVencidasTreinta(obligacionId: String) = {
    obnVencidas match {
      case x if x.contains(obligacionId) => x updated (obligacionId, false) //la modifico si existe
      case x => x + (obligacionId -> false) //la creo sino existe
    }
  }

  private def changeState(event: ObjetoEvents): ObjetoState =
    event match {
      case cmd: ObjetoEvents.ObjetoUpdatedCotitulares =>
        copy(
          sujetos = cmd.cotitulares,
          isBaja = false
        )
      case ObjetoEvents.ObjetoAddedExencion(deliveryId, sujetoId, objetoId, tipoObjeto, exencion) =>
        copy(
          exenciones = exenciones + exencion,
          isBaja = false
        )
      // TODO: check si agregamos el deliveryId en objeto desde sujeto
      case evt: ObjetoEvents.ObjetoUpdatedFromSujeto =>
        val dmnDescripcion_ = dmnDescripcion
        val exclusionAnteriorSujeto = exclusionSujeto
        val exclusionNuevaSujeto = evt.exclusionSUjeto

        def cambiarRazonPorSujeto(exclusionNueva: Option[String],
                                  exclusionAnterior: Option[String],
                                  dmnDescripcionActual: Option[String],
                                  dmnDescripcionSujeto: Option[String]): Option[String] = {
          (exclusionNueva, exclusionAnterior) match {
            case (Some(_), None) => dmnDescripcionSujeto
            case (Some(_), Some(_)) => dmnDescripcionSujeto
            case (None, Some(_)) => dmnDescripcionAnteriorPorSujeto.orElse(dmnDescripcionActual)
            case _ => dmnDescripcionActual
          }
        }

        copy(
          tiene30Sujeto = Some(evt.tiene30Sujeto),
          exclusionSujeto = evt.exclusionSUjeto,
          dmnDescripcionAnteriorPorSujeto = {
            if (exclusionNuevaSujeto.isDefined && exclusionAnteriorSujeto.isEmpty) {
              dmnDescripcion_
            } else {
              dmnDescripcionAnteriorPorSujeto
            }
          },
          dmnDescripcion = cambiarRazonPorSujeto(
            exclusionNuevaSujeto,
            exclusionAnteriorSujeto,
            dmnDescripcion_,
            evt.dmnDescripcionSujeto
          )
        )

      case evt: AplicarDescuentoUpdated =>
        copy(aplicarDescuento = evt.aplicarDescuento)
      // TODO: check when an object with multiple owners changes its exclusions.
      case evt: ObjetoEvents.UpdatedState30ObjetoFromObjVinculo =>
        // Si tiene30ObjetoVinculo = false Y no tiene obligaciones propias heredar la penalizacion
        val tiene30ObjetoCalculado = if (!evt.tiene30ObjetoVinculo && obligaciones.isEmpty) {
          false
        } else {
          tiene30Objeto // Sino mantener la marca actual
        }
        copy(
          tiene30ObjetoVinculo = evt.tiene30ObjetoVinculo,
          tiene30Objeto = tiene30ObjetoCalculado,  // ← ACTUALIZAR tiene30Objeto
          exclusionObjeto = evt.exclusionObjetoVinculo
        )
      case evt: ObjetoEvents.ObjetoUpdatedFromTri =>
        val dmnDescripcion_ = dmnDescripcion
        val exclusionAnterior = exclusionObjeto
        val exclusionNueva = evt.registro.SOJ_TIPO_EXCLUSION.getOrElse("")

        def cambiarRazon(tipo: String, dmnDescripcionActual: String, exclusionAnterior: Option[String]): Option[String] = {
          tipo match {
            case x if x.contains("E")  => Some("Objeto Excluido")
            case x if x.contains("NE") => Some("Objeto No Excluido")
            case x if x.contains("C")  => Some("Objeto Condicional")
            case "" if exclusionAnterior.isDefined => {
              dmnDescripcionAnterior.orElse(Some(dmnDescripcionActual))
            }
            case _ => Some(dmnDescripcionActual)
          }
        }

        copy(
          sujetoResponsable = evt.sujetoResponsable match {
            case Some(value) => Some(value)
            case None => this.sujetoResponsable
          },
          isResponsable = evt.isResponsable.getOrElse(false),
          registro = Some(evt.registro),
          sujetos = sujetos + evt.sujetoId,
          isAdheridoDebito = evt.isAdheridoDebito.getOrElse(false),
          isBaja = false,
          clasificacionObjeto = evt.clasificacionObjeto.getOrElse("2"),
          resulDmn = evt.resultDmn,
          exclusionObjeto = evt.registro.SOJ_TIPO_EXCLUSION match {
            case x if x.contains("E") => Some("E")
            case x if x.contains("NE") => Some("NE")
            case x if x.contains("C") => Some("C")
            case _ => None
          },
          dmnDescripcionAnterior = {
            if (exclusionNueva.nonEmpty && exclusionAnterior.isEmpty) {
              dmnDescripcion
            } else {
              dmnDescripcionAnterior
            }
          },
          dmnDescripcion = cambiarRazon(exclusionNueva, dmnDescripcion_.getOrElse(""), exclusionAnterior)
        )

      case evt: ObjetoEvents.ObjetoUpdatedFromAnt =>
        copy(
          sujetoResponsable = evt.sujetoResponsable match {
            case Some(value) => Some(value)
            case None => this.sujetoResponsable
          },
          isResponsable = evt.isResponsable.getOrElse(false),
          registro = Some(evt.registro),
          sujetos = sujetos + evt.sujetoId,
          isAdheridoDebito = evt.isAdheridoDebito.getOrElse(false),
          isBaja = false
        )
      case ObjetoEvents.ObjetoUpdatedFromObligacion(_, sujetoId, _, _, _, obligacionId, saldoObligacion, _, _, _, _, dmnNumero, dmnDescripcion) =>
        val _obnVencidas = validExitsObnVencidas(obligacionId)
        val obligacionesSaldo_ = obligacionesSaldo + (obligacionId -> saldoObligacion)
        val diff: Boolean = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
        copy(
          saldo = obligacionesSaldo_.values.sum,
          obligaciones = obligaciones + obligacionId,
          obligacionesSaldo = obligacionesSaldo_,
          sujetos = sujetos + sujetoId,
          isBaja = false,
          obnVencidas = _obnVencidas,
          tiene30Objeto = diff,
          ultimo30Objeto = ultimo30Objeto + ((event.deliveryId.toString, diff)),
          dmnNumero = dmnNumero,
          dmnDescripcion = dmnDescripcion
        )

      case evt: ObjetoEvents.ObjetoUpdatedFromObnTreintaProciento =>
        val _obnVencidas = validExitsObnVencidasTreinta(evt.obligacionId)
        val diff = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
        copy(
          obnVencidas = _obnVencidas,
          tiene30Objeto = diff,
          tiene30ObjetoVinculo = tiene30ObjetoVinculo, //todo agregue aca
          dmnNumero = evt.dmnNumero,
          dmnDescripcion = evt.dmnDescripcion
        )
      case evt: ObjetoEvents.ObjetoSnapshotPersisted =>
        copy(
          saldo = evt.saldo,
          sujetos = evt.cotitulares,
          sujetoResponsable = evt.sujetoResponsable,
          obligacionesSaldo = evt.obligacionesSaldo,
          tags = evt.tags,
          isBaja = false,
          cuotas = evt.cuotas
        )
      case evt: ObjetoEvents.ObjetoTagAdded =>
        copy(tags = tags + evt.tagAdded, isBaja = false)
      case evt: ObjetoEvents.ObjetoTagRemoved =>
        copy(tags = tags - evt.tagRemoved, isBaja = false)

      case evt: ObjetoEvents.ObjetoBajaSet =>
        copy(
          sujetoResponsable = evt.sujetoResponsable match {
            case Some(value) => Some(value)
            case None => this.sujetoResponsable
          },
          isResponsable = evt.isResponsable.getOrElse(false),
          registro = Some(evt.registro),
          isBaja = true
        )
      case evt: ObjetoEvents.ObjetoRemovedObligacion =>
        val obligacionesSaldo_ = obligacionesSaldo - (evt.obligacionId)
        if (evt.cuota.isEmpty || evt.cuota.get.toInt < 0 || evt.cuota.get.toInt > 12) {
          val _obnVencidas = obnVencidas - evt.obligacionId
          val diff = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
          copy(
            saldo = obligacionesSaldo_.values.sum,
            obligaciones = obligaciones - evt.obligacionId,
            obligacionesSaldo = obligacionesSaldo_,
            obnVencidas = _obnVencidas,
            tiene30Objeto = diff,
            dmnNumero = evt.dmnNumero,
            dmnDescripcion = evt.dmnDescripcion
          ) //todo ver aca como es para cuando pago la obligacion se cambie el state de los objetos
        } else {
          val cuotaIndex_ = evt.cuota.get.toInt
          val cuotasPagadas_ = cuotas.updated(cuotaIndex_, true)
          val _obnVencidas = obnVencidas - evt.obligacionId
          val diff = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
          copy(
            saldo = obligacionesSaldo_.values.sum,
            obligaciones = obligaciones - evt.obligacionId,
            obligacionesSaldo = obligacionesSaldo_,
            cuotas = cuotasPagadas_,
            obnVencidas = _obnVencidas,
            tiene30Objeto = diff,
            dmnNumero = evt.dmnNumero,
            dmnDescripcion = evt.dmnDescripcion
          )
        }
      case evt: ObjetoEvents.RemovedObjetoFromObligacion =>
        val _obnVencidas =
          if (obnVencidas.contains(evt.obligacionId)) {
            obnVencidas - evt.obligacionId
          } else {
            obnVencidas
          }
        val diff = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
        copy(
          obnVencidas = _obnVencidas,
          tiene30Objeto = diff
        )
      case evt =>
        log.warn(s"Unexpected event at ObjetoState ${evt}")
        this
    }

  def empty = ObjetoState()

}
