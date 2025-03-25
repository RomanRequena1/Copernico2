package consumers.no_registral.objeto.application.helper

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import cqrs.untyped.command.StateParcial
import org.slf4j.{Logger, LoggerFactory}

import java.lang.reflect.Field
import java.time.LocalDateTime

class StateParcialObjeto extends StateParcial {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Determina si un evento es un state parcial
   * Este método identifica si el evento contiene solo algunos campos
   * en lugar de un objeto completo
   * @param evento Evento a analizar
   * @return true si es un state parcial, false si es un objeto completo
   */
  def isStateParcial(evento: ObjetoExternalDto): Boolean = {
    // Campos considerados importantes para un objeto completo
    val missingFields = List(
      evento.SOJ_DESCRIPCION.isEmpty,
      evento.SOJ_CAT_SOJ_ID.isEmpty,
      evento.SOJ_FECHA_INICIO.isEmpty,
      evento.SOJ_CANAL_ORIGEN.isEmpty,
      evento.SOJ_ID_EXTERNO.isEmpty
    )

    // Si faltan muchos campos importantes, consideramos que es un state parcial
    missingFields.count(missing => missing) >= 3
  }

  /**
   * Determina si un evento debe ser filtrado (no procesado)
   * @param evento Evento a verificar
   * @param estado Estado actual
   * @return true si el evento debe ser filtrado, false si debe procesarse
   */
  def shouldFilterEvent(evento: ObjetoExternalDto, estado: Option[ObjetoExternalDto]): Boolean = {
    // LÓGICA SIMPLIFICADA:
    // 1. Si es un objeto completo: siempre procesar (no filtrar)
    // 2. Si es un state parcial y ya existe: siempre procesar (no filtrar)
    // 3. Si es un state parcial y no existe: filtrar (no procesar)

    val isPartial = isStateParcial(evento)
    val existsInDB = estado.isDefined

    // Lógica simplificada
    isPartial && !existsInDB
  }

  /**
   * Casos de otros atributos para Objeto:
   * 1- Evento con oAtri en None, sin estado
   * 2- Evento con oAtri en None, con estado (Independiente del valor de oAtri en estado)
   * 3- Evento con oAtri con valores, sin estado
   * 4- Evento con oAtri con valores, con estado
   * 4.1- Con estado oAtri en None
   * 4.2- Con estado oAtri con valores
   *
   * @param evento Evento de Objeto
   * @param estado Estado de Objeto
   * @param campoActualizar Tipo Field para manipular nombre y valor del campo
   * @param eventoNuevo Nuevo registro a persistir en el estado
   */
  override def spOtrosAtributos(evento: Object, estado: Option[Object], campoActualizar: Field, eventoNuevo: Object): Unit = {
    val miEvento: ObjetoExternalDto = evento match {
      case e: ObjetoExternalDto => e
    }

    val miEstado: Option[ObjetoExternalDto] = estado match {
      case Some(e: ObjetoExternalDto) => Some(e)
      case _ => None
    }

    // Verificar si el evento debe procesarse según nuestra lógica simplificada
    if (shouldFilterEvent(miEvento, miEstado)) {
      // No realizar ninguna actualización para eventos state parcial sin estado existente
      log.debug(s"Ignorando spOtrosAtributos para evento state parcial sin vínculo existente | objetoId: ${miEvento.SOJ_IDENTIFICADOR}")
      return
    }

    miEvento.SOJ_OTROS_ATRIBUTOS match {
      case None =>
        estado match {
          // Caso 1- Evento con oAtri en None, sin estado
          case None => ()
          // Caso 2- Evento con oAtri en None, con estado (Independiente del valor de oAtri en estado)
          case Some(oEstado: ObjetoExternalDto) => campoActualizar.set(eventoNuevo, oEstado.SOJ_OTROS_ATRIBUTOS)
          case _ => ()
        }
      case Some(oAtriEvento) =>
        estado match {
          // Caso 3- Evento con oAtri con valores, sin estado
          case None => {
            val otrosAtributosEventoDetalles = oAtriEvento.SOJ_DETALLES.head
            // Validar campos en defaults para evento
            val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEventoDetalles, None)
            campoActualizar.set(eventoNuevo, Some(ListDetallesObjeto(List(otrosAtributosUpdated match { case o: DetallesObjeto => o}))))
          }
          // Caso 4- Evento con oAtri con valores, con estado
          case Some(oEstado: ObjetoExternalDto) =>
            oEstado.SOJ_OTROS_ATRIBUTOS match {
              // Caso 4.1- Con estado oAtri en None
              case None => {
                val otrosAtributosEventoDetalles = oAtriEvento.SOJ_DETALLES.head
                val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEventoDetalles, None)
                campoActualizar.set(eventoNuevo, Some(ListDetallesObjeto(List(otrosAtributosUpdated match { case o: DetallesObjeto => o }))))
              }
              // Caso 4.2- Con estado oAtri con valores
              case Some(oAtriEstado) => {
                val otrosAtributosEvento = miEvento.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head
                val otrosAtributosEstado = oAtriEstado.SOJ_DETALLES.head
                val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEvento, Some(otrosAtributosEstado))
                campoActualizar.set(eventoNuevo, Some(ListDetallesObjeto(List(otrosAtributosUpdated.asInstanceOf[DetallesObjeto]))))
              }
            }
          case _ => ()
        }
    }
  }
}

object StateParcialObjeto {
  val SPO = new StateParcialObjeto()

  /**
   * Método principal para realizar el state parcial en un objeto
   * @param evento Evento a procesar
   * @param estado Estado existente (si existe)
   * @return Objeto actualizado después de aplicar state parcial
   */
  def stateParcialCC(evento: ObjetoExternalDto, estado: Option[ObjetoExternalDto]): ObjetoExternalDto = {
    // Si debemos filtrar el evento según nuestra lógica simplificada
    if (SPO.shouldFilterEvent(evento, estado)) {
      return evento  // Retorna el evento sin cambios (no se persistirá)
    }

    try {
      // Procesar normalmente si pasa la validación
      val result = SPO.stateParcialCC(evento, estado).asInstanceOf[ObjetoExternalDto]
      result
    } catch {
      case e: Exception =>
        SPO.log.error(s"ERROR en stateParcialCC para objetoId: ${evento.SOJ_IDENTIFICADOR}: ${e.getMessage}", e)
        throw e
    }
  }

  /**
   * Verifica si un evento debe ser procesado o no
   * @param evento Evento a verificar
   * @param estado Estado actual
   * @return true si el evento debe procesarse, false en caso contrario
   */
  def shouldProcessEvent(evento: ObjetoExternalDto, estado: Option[ObjetoExternalDto]): Boolean = {
    // LÓGICA SIMPLIFICADA:
    // 1. Si es un objeto completo: siempre procesar (retornar true)
    // 2. Si es un state parcial y ya existe en BD: siempre procesar (retornar true)
    // 3. Si es un state parcial y no existe en BD: no procesar (retornar false)

    val isPartial = SPO.isStateParcial(evento)
    val existsInDB = estado.isDefined

    // Lógica inversa a shouldFilterEvent
    !isPartial || existsInDB
  }
}