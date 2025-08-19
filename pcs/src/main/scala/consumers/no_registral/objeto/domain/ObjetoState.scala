package consumers.no_registral.objeto.domain

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import ddd.{eventCounterMax, AbstractState}
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
    clasificacionObjeto: String = "2",
    tiene30Sujeto: Option[Boolean] = None,
    aplicarDescuento: Option[Boolean] = None,
    resulDmn: Option[Int] = None,
    obnVencidas: Map[String, Boolean] = Map.empty,
    deuda30Objeto: Boolean = true,
    tipoExclusion: String = "",
    exclusionObjeto: Option[String] = None,
    exclusionObjetoVinculo: Option[String] = None,
    isDeleted: Boolean = false
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
      case evt: ObjetoEvents.ObjetoDeleted =>
        copy(
          isDeleted = true,
          obligacionesSaldo = Map.empty,
          obligaciones = Set.empty,
          saldo = BigDecimal(0),
          obnVencidas = Map.empty,
          isBaja = true
        )
      // TODO: check si agregamos el deliveryId en objeto desde sujeto
      case evt: ObjetoEvents.ObjetoUpdatedFromSujeto =>
        copy(tiene30Sujeto = Some(evt.tiene30Sujeto))
      // TODO: check when an object with multiple owners changes its exclusions.
      case evt: ObjetoEvents.UpdatedState30ObjetoFromObjVinculo =>
        copy(
          tiene30ObjetoVinculo = evt.tiene30ObjetoVinculo, //todo CAMBIE ACA
          // Este pisaba a todos los tiene30objeto de los VSO, deberia guardarse solo en tiene30ObjVinculo
          //          tiene30Objeto = diffCurrentStateAndNewStateTest(obnVencidas, _tiene30ObjetoVinculo),
          exclusionObjeto = evt.exclusionObjetoVinculo
        )
      case evt: ObjetoEvents.ObjetoUpdatedFromTri =>
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
          }
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
      case ObjetoEvents.ObjetoUpdatedFromObligacion(_, sujetoId, _, _, _, obligacionId, saldoObligacion, _, _, _, _) =>
        val _obnVencidas = validExitsObnVencidas(obligacionId)
        val obligacionesSaldo_ = obligacionesSaldo + (obligacionId -> saldoObligacion)
        val diff = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
        copy(
          saldo = obligacionesSaldo_.values.sum,
          obligaciones = obligaciones + obligacionId,
          obligacionesSaldo = obligacionesSaldo_,
          sujetos = sujetos + sujetoId,
          isBaja = false,
          obnVencidas = _obnVencidas,
          tiene30Objeto = diff
        )

      case evt: ObjetoEvents.ObjetoUpdatedFromObnTreintaProciento =>
        val _obnVencidas = validExitsObnVencidasTreinta(evt.obligacionId)
        val diff = diffCurrentStateAndNewState(_obnVencidas, tiene30Objeto)
        copy(
          obnVencidas = _obnVencidas,
          tiene30Objeto = diff,
          tiene30ObjetoVinculo = tiene30ObjetoVinculo //todo agregue aca
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
            tiene30Objeto = diff
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
            tiene30Objeto = diff
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
