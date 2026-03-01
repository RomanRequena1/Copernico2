package consumers.no_registral.tranferencia.domain

import ddd.AbstractState
import org.slf4j.LoggerFactory
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
                                     // FIX: Cambiado de false a true
                                     // Un objeto sin vínculos procesados se considera "sin deuda en vínculos" por default
                                     // Antes: false (se interpretaba como "tiene deuda" cuando en realidad era "sin información")
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

  override val log = LoggerFactory.getLogger(this.getClass)

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
                                         newMapVinculo: Map[Vinculo, VinculoCotitular],
                                         newMapTransf: Map[Vinculo, VinculoCotitular]
                                       ): Boolean = {
    val resultado = if (newMapTransf.isEmpty) {
      if (newMapVinculo.isEmpty) {
        // FIX: Si no hay vínculos, retornar true (sin deuda en vínculos)
        true
      } else if (newMapVinculo.forall(_._2.tiene30Objeto)) {
        true
      } else {
        false
      }
    } else {
      if (newMapVinculo.forall(_._2.tiene30Objeto) && newMapTransf.forall(_._2.tiene30Objeto)) true else false
    }

    // LOG: Registrar el cálculo del 30% desde vínculos
    log.info(
      s"[VINCULO-CALC-30] objetoId=$objetoId - " +
        s"mapVinculo.size=${newMapVinculo.size}, mapTransf.size=${newMapTransf.size}, " +
        s"mapVinculo.valores=${newMapVinculo.map { case (k, v) => s"${k.sujetoId}:${v.tiene30Objeto}" }.mkString(",")}, " +
        s"resultado=$resultado"
    )

    resultado
  }

  /**
   * 1. Si el vinculo ya existe en el map, se actualiza el valor del vinculo
   * 2. Si el vinculo no existe en el map, se agrega el vinculo al map
   */
  private def UpdateObjVinculo(vinculo: Vinculo, vinculoCotitular: VinculoCotitular) = {
    mapVinculo match {
      case x if x.contains(vinculo) =>
        x updated (vinculo, vinculoCotitular)
      case x =>
        x + (vinculo -> vinculoCotitular)
    }
  }

  /**
   * Si mapTransf no contiene el vinculo, se agrega el vinculo al map solo si el vinculo es responsable y tiene 30 es false
   * Si mapTransf contiene el vinculo, se verifica si el valor del vinculo es igual al valor del vinculoCotitular,
   * si es igual no se hace nada, si es distinto se elimina el vinculo del map
   */
  private def updateMapTransf(
                               vinculo: Vinculo,
                               vinculoCotitular: VinculoCotitular
                             ): Map[Vinculo, VinculoCotitular] = {
    if (mapTransf.filterNot(x => !x._1.sujetoId.equals(vinculo.sujetoId)).isEmpty) {
      if (vinculoCotitular.isResponsable.get && vinculoCotitular.tiene30Objeto.equals(false)) {
        mapTransf + (vinculo -> vinculoCotitular)
      } else {
        mapTransf
      }
    } else {
      val vinculoOld = mapTransf.find(e => e._1.equals(vinculo))
      if (vinculoOld.isDefined) {
        if (vinculoOld.get._2.tiene30Objeto.equals(vinculoCotitular.tiene30Objeto))
          mapTransf
        else
          mapTransf - vinculo
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
        val vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.titularidad, evt.estadoObj)

        val estaEnVinculo = mapVinculo.contains(vinculo)
        val estaEnTransf = mapTransf.contains(vinculo)

        val newMapVinculo = if (estaEnVinculo) {
          UpdateObjVinculo(vinculo, vinculoCotitular)
        } else if (!estaEnTransf) {
          mapVinculo + (vinculo -> vinculoCotitular)
        } else {
          mapVinculo
        }

        val newMapTransf = if (estaEnTransf) {
          mapTransf.updated(vinculo, vinculoCotitular)
        } else {
          mapTransf
        }

        val tiene30ObjetoVinculoAnterior = tiene30ObjetoVinculo
        val tiene30ObjetoVinculoNuevo = calcular30desdeMapVinculo(newMapVinculo, newMapTransf)

        // LOG: Registrar cambio de estado
        log.info(
          s"[VINCULO-UPDATE] objetoId=${evt.objetoId}, sujetoId=${evt.sujetoId} - " +
            s"tiene30Objeto=${evt.tiene30Objeto}, " +
            s"tiene30ObjetoVinculo: $tiene30ObjetoVinculoAnterior -> $tiene30ObjetoVinculoNuevo, " +
            s"estaEnVinculo=$estaEnVinculo, estaEnTransf=$estaEnTransf"
        )

        // CORRECCION: Usar el método centralizado para actualizar DMN
        val (dmnNumeroNuevo, dmnDescripcionNuevo) = actualizarDmn(evt.dmnNumero, evt.dmnDescripcion, evt.objetoId)

        copy(
          lastDeliveryIdByEvents = evt.deliveryId,
          tiene30ObjetoVinculo = tiene30ObjetoVinculoNuevo,
          mapVinculo = newMapVinculo,
          mapTransf = newMapTransf,
          exclusionObjetoVinculo = evt.exclusionObjeto,
          dmnNumeroVinculo = dmnNumeroNuevo,
          dmnDescripcionVinculo = dmnDescripcionNuevo,
          idExterno = evt.idExterno
        )

      case evt: ObjetoVinculoEvent.CreatedTransfVinculoObjetoFromObj =>
        val vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val vinculoCotitular = VinculoCotitular(evt.tiene30Objeto, evt.isResponsable, evt.titularidad, evt.estadoObj)

        val vinculosAnteriores = mapVinculo.filter(v => v._1.objetoId.equals(evt.objetoId))
        val (nuevoMapTransf, nuevoMapVinculo) = evt.estadoObj match {
          case Some("TRANSF") =>
            val updatedMapTransf = vinculosAnteriores.foldLeft(mapTransf) {
              case (acc, (v, cotitular)) =>
                acc + (v -> cotitular.copy(estado = Some("TRANSF")))
            }
            val mapVinculoLimpio = mapVinculo.filterNot(v => v._1.objetoId.equals(evt.objetoId))
            val updatedMapVinculo = mapVinculoLimpio + (vinculo -> vinculoCotitular)
            (updatedMapTransf, updatedMapVinculo)

          case Some("ESTADO2") =>
            val updatedMapTransf = vinculosAnteriores.foldLeft(mapTransf) {
              case (acc, (v, cotitular)) =>
                acc + (v -> cotitular.copy(estado = Some("ESTADO2")))
            }
            val mapVinculoLimpio = mapVinculo.filterNot(v => v._1.objetoId.equals(evt.objetoId))
            val updatedMapVinculo = mapVinculoLimpio + (vinculo -> vinculoCotitular)
            (updatedMapTransf, updatedMapVinculo)

          case _ =>
            val updatedMapVinculo = mapVinculo + (vinculo -> vinculoCotitular)
            (mapTransf, updatedMapVinculo)
        }

        // CORRECCION: Usar el método centralizado para actualizar DMN
        val (dmnNumeroNuevo, dmnDescripcionNuevo) = actualizarDmn(evt.dmnNumero, evt.dmnDescripcion, evt.objetoId)

        val tiene30ObjetoVinculoAnterior = tiene30ObjetoVinculo
        val tiene30ObjetoVinculoNuevo = calcular30desdeMapVinculo(nuevoMapVinculo, nuevoMapTransf)

        // LOG: Registrar creación de transferencia
        log.info(
          s"[VINCULO-TRANSF-CREATE] objetoId=${evt.objetoId}, sujetoId=${evt.sujetoId} - " +
            s"tiene30Objeto=${evt.tiene30Objeto}, estadoObj=${evt.estadoObj}, " +
            s"tiene30ObjetoVinculo: $tiene30ObjetoVinculoAnterior -> $tiene30ObjetoVinculoNuevo"
        )

        copy(
          tiene30ObjetoVinculo = tiene30ObjetoVinculoNuevo,
          mapVinculo = nuevoMapVinculo,
          mapTransf = nuevoMapTransf,
          idExterno = evt.idExterno,
          exclusionObjetoVinculo = evt.exclusionObjeto,
          dmnNumeroVinculo = dmnNumeroNuevo,
          dmnDescripcionVinculo = dmnDescripcionNuevo
        )

      case evt: ObjetoVinculoEvent.RemovedVinculoObjetoFromObj =>
        val vinculo = Vinculo(evt.sujetoId, evt.objetoId, evt.tipoObj)
        val newMapVinculo = if (mapVinculo.contains(vinculo)) mapVinculo - vinculo else mapVinculo
        val newMapTransf = if (mapTransf.contains(vinculo)) mapTransf - vinculo else mapTransf
        val tiene30ObjetoVinculoAnterior = tiene30ObjetoVinculo
        val tiene30ObjetoVinculoNuevo = calcular30desdeMapVinculo(newMapVinculo, newMapTransf)

        // LOG: Registrar eliminación de vínculo
        log.info(
          s"[VINCULO-REMOVE] objetoId=${evt.objetoId}, sujetoId=${evt.sujetoId} - " +
            s"tiene30ObjetoVinculo: $tiene30ObjetoVinculoAnterior -> $tiene30ObjetoVinculoNuevo"
        )

        copy(
          tiene30ObjetoVinculo = tiene30ObjetoVinculoNuevo,
          mapVinculo = newMapVinculo,
          mapTransf = newMapTransf
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