package consumers.no_registral.sujeto.domain

import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.domain.SujetoEvents.{SujetoUpdatedFromObjeto, SujetoUpdatedFromObjetoTreintaPorciento}
import ddd.{AbstractState, eventCounterMax}
import serialization.CbroSerialization

import java.time.LocalDateTime

final case class SujetoState(
    saldo: BigDecimal = 0,
    saldoTotal: BigDecimal = 0,
    fechaUltSaldo: LocalDateTime = LocalDateTime.MIN,
    saldoObjetos: Map[String, BigDecimal] = Map.empty,
    saldoObligaciones: Map[String, BigDecimal] = Map.empty,
    objetos: Set[(String, String)] = Set.empty,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    registro: Option[SujetoExternalDto] = None,
    lastDeliveryIdByEvents: BigInt = 0,
    eventCounter: Int = 0,
    cuotas: List[Boolean] =
      List(false, false, false, false, false, false, false, false, false, false, false, false, false),
    tiene30Sujeto: Boolean = true,
    objVencidas: Map[String, (Boolean, String)] = Map.empty, //todo este ("idObjeto" -> (valor30%, "tipoObjeto(sale del alta objeto) "))
    diffStates: Boolean = false, //todo este ("IBG456" -> (true, "2")
    lastInternalDeliveryId: BigInt = 0,
    exclusionSujeto: Option[String] = None,
    dmnDescripcionAnterior: Option[String] = None, // ← NUEVO
    dmnDescripcion: Option[String] = None,
    deliveryIdObligacion: Option[BigInt] = None,
    idExterno: Option[String] = None,
    ) extends AbstractState[SujetoEvents] with CbroSerialization {


  def +(event: SujetoEvents): SujetoState = {
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
      eventCounter = c
    )*/
  }

  private def validExitsObjVencidas(objetoId: String, clasificacionObjeto: String) = {

    objVencidas match {
      //case x if x.contains(objetoId) && x(objetoId)._2.equals("") => x updated (objetoId, (true, clasificacionObjeto))
      case x if x.contains(objetoId) => x updated (objetoId, (true, clasificacionObjeto))
      case x => x + (objetoId -> ((true, clasificacionObjeto)))
    }

    //if (objVencidas.contains(objetoId)) objVencidas else objVencidas + (objetoId -> (true, clasificacionObjeto))
  }
  private def diffCurrentStateAndNewState(currentObjVnecidad: Map[String, (Boolean, String)],
                                          newObjVnecidad: Map[String, (Boolean, String)],
                                          deuda30Sujeto: Boolean) = {
    val map = newObjVnecidad.filter(obj => !obj._2._2.equals("1"))
    println(s"[DEBUG-SUJETO-DIFF] objVencidas.size=${newObjVnecidad.size}, filtrado(clasif!=1).size=${map.size}")
    println(s"[DEBUG-SUJETO-DIFF] valores=${map.map { case (k, v) => s"$k:(${v._1},${v._2})" }.mkString(", ")}")
    println(s"[DEBUG-SUJETO-DIFF] forall(_._1)=${map.values.forall(_._1)}")
    if (map.values.forall(_._1)) {
      val newTupla = (true, tiene30Sujeto.equals(true))
      println(s"[DEBUG-SUJETO-DIFF] RESULTADO: tiene30Sujeto=true")
      newTupla
    } else {
      val newTupla = (false, tiene30Sujeto.equals(false))
      println(s"[DEBUG-SUJETO-DIFF] RESULTADO: tiene30Sujeto=false")
      newTupla
    }
  }
  private def validExitsObjVencidasTreinta(objetoId: String, clasificacionObjeto: String) = {
    objVencidas match {
      //case x if x.contains(objetoId) && x(objetoId)._2.equals("") => x updated (objetoId, (false, clasificacionObjeto))
      case x if x.contains(objetoId) => x updated (objetoId, (false, clasificacionObjeto))
      case x => x + (objetoId -> ((false, clasificacionObjeto)))
    }

    //if (objVencidas.contains(objetoId)) objVencidas updated (objetoId , (false, objVencidas(objetoId)._2 )) else objVencidas + (objetoId -> (false, clasificacionObjeto))
  }
  private def changeState(event: SujetoEvents): SujetoState =
    event match {
      case SujetoEvents.SujetoUpdatedFromTri(_, _, registro) =>
        val dmnDescripcion_ = dmnDescripcion
        val exclusionAnterior = exclusionSujeto
        val exclusionNueva = registro.SUJ_TIPO_EXCLUSION.getOrElse("")

        def cambiarRazon(tipo: String, dmnDescripcionActual: String, exclusionAnterior: Option[String]): Option[String] = {
          tipo match {
            case x if x.contains("E")  => Some("Sujeto Excluido")
            case x if x.contains("NE") => Some("Sujeto No Excluido")
            case x if x.contains("C")  => Some("Sujeto Condicional")
            case "" if exclusionAnterior.isDefined => {
              dmnDescripcionAnterior.orElse(Some(dmnDescripcionActual))
            }
            case _ => Some(dmnDescripcionActual)
          }
        }

        val tiene30SujetoCalculado = registro.SUJ_TIPO_EXCLUSION match {
          case Some(tipo) if tipo.nonEmpty =>
            tipo match {
              case "E"  => true
              case "NE" => false
              case "C"  => true
              case _ => tiene30Sujeto
            }

          case _ =>
            // solo usar los que son clasificacion 2
            val objetosClasif2 = objVencidas.filter(_._2._2 == "2")

            if (objetosClasif2.isEmpty) {
              true
            } else {
              objetosClasif2.values.forall(_._1)
            }
        }
        copy(
          registro = Some(registro),
          exclusionSujeto = registro.SUJ_TIPO_EXCLUSION match {
            case Some(tipo) if tipo.nonEmpty =>
              tipo match {
                case "E"  => Some("E")
                case "NE" => Some("NE")
                case "C"  => Some("C")
                case _ => None
              }
            case _ => None
          },
          tiene30Sujeto = tiene30SujetoCalculado,
          dmnDescripcionAnterior = {
            if (exclusionNueva.nonEmpty && exclusionAnterior.isEmpty) {
              dmnDescripcion
            } else {
              dmnDescripcionAnterior
            }
          },
          dmnDescripcion = cambiarRazon(exclusionNueva, dmnDescripcion_.getOrElse(""), exclusionAnterior)
        )

      case SujetoEvents.SujetoUpdatedFromAnt(_, _, registro) =>
        copy(
          registro = Some(registro)
        )
      case SujetoEvents.SujetoUpdatedFromObjeto(deliveryId,
                                                _,
                                                objetoId,
                                                tipoObjeto,
                                                saldoObjeto,
                                                _saldoObligaciones,
                                                clasificacionObjeto,
                                                idExterno,
                                                deliveryIdObligacion) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos + (objetoKey -> saldoObjeto)
        val _objVencidas = validExitsObjVencidas(objetoId, clasificacionObjeto)
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, tiene30Sujeto)

        copy(
          objetos = objetos + ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones + (objetoKey -> _saldoObligaciones),
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          tiene30Sujeto = diff._1,
          diffStates = diff._2,
          idExterno = idExterno,
          deliveryIdObligacion = deliveryIdObligacion
        )

      case SujetoEvents.SujetoUpdatedFromObjetoAnt(deliveryId,
                                                   _,
                                                   objetoId,
                                                   tipoObjeto,
                                                   saldoObjeto,
                                                   _saldoObligaciones,
                                                   clasificacionObjeto) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos + (objetoKey -> saldoObjeto)
        val _objVencidas = validExitsObjVencidas(objetoId, clasificacionObjeto)
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, tiene30Sujeto)

        copy(
          objetos = objetos + ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones + (objetoKey -> _saldoObligaciones),
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          tiene30Sujeto = diff._1,
          diffStates = diff._2,
        )

      case SujetoEvents.SujetoUpdatedFromObjetoTreintaPorciento(deliveryId,
                                                                _,
                                                                objetoId,
                                                                tipoObjeto,
                                                                saldoObjeto,
                                                                _saldoObligaciones,
                                                                clasificacionObjeto,
                                                                idExterno,
                                                                deliveryIdObligacion) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos + (objetoKey -> saldoObjeto)
        val _objVencidas = validExitsObjVencidasTreinta(objetoId, clasificacionObjeto)
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, tiene30Sujeto)

        copy(
          objetos = objetos + ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones + (objetoKey -> _saldoObligaciones),
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          tiene30Sujeto = diff._1,
          diffStates = diff._2,
          idExterno = idExterno,
          deliveryIdObligacion = deliveryIdObligacion
        )
      case SujetoEvents.SujetoBajaFromObjetoSet(deliveryId, _, objetoId, tipoObjeto) =>
        val objetoKey = s"$objetoId|$tipoObjeto"
        val _saldoObjetos = saldoObjetos - objetoKey
        val _objVencidas = objVencidas - objetoId
        val diff = diffCurrentStateAndNewState(objVencidas, _objVencidas, tiene30Sujeto)
        copy(
          objetos = objetos - ((objetoId, tipoObjeto)),
          saldoObjetos = _saldoObjetos,
          saldo = _saldoObjetos.values.sum,
          saldoObligaciones = saldoObligaciones - objetoKey,
          lastInternalDeliveryId = deliveryId,
          objVencidas = _objVencidas,
          diffStates = diff._2,
          tiene30Sujeto = diff._1
        )
      case evt: SujetoEvents.SujetoSnapshotPersisted =>
        copy(
          saldo = evt.saldo
        )
      case _ => this
    }
}
