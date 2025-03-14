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
   * Determina si un evento proviene de semáforo
   */
  def esEventoSemaforo(objetoDto: ObjetoExternalDto): Boolean = {
    // Verificar si es evento de semáforo (tiene SOJ_SEMAFORO_COLOR)
    objetoDto.SOJ_OTROS_ATRIBUTOS.exists { atributos =>
      atributos.SOJ_DETALLES.exists { detalle =>
        detalle.SOJ_SEMAFORO_COLOR.isDefined
      }
    }
  }

  /**
   * Determina si un evento es un state parcial (no tiene todos los campos)
   * Esta lógica puede ajustarse según los campos que se consideren obligatorios
   */
  def esStateParcial(objetoDto: ObjetoExternalDto): Boolean = {
    // Aquí definimos qué hace que un objeto sea considerado "parcial"
    // Por ejemplo, si faltan ciertos campos críticos
    objetoDto.SOJ_SUBTIPO.isEmpty ||
      objetoDto.SOJ_ESTADO.isEmpty ||
      objetoDto.SOJ_TITULARIDAD.isEmpty
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

    val hayEstadoPrevio = estado.isDefined

    // Si es evento de semáforo o state parcial y no hay estado previo, descartar
    if ((esEventoSemaforo(miEvento) || esStateParcial(miEvento)) && !hayEstadoPrevio) {
      log.warn(s"Descartando actualización de evento sin vínculo existente: ${if(esEventoSemaforo(miEvento)) "semáforo" else "state parcial"}")
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

  def stateParcialCC(evento: ObjetoExternalDto, estado: Option[ObjetoExternalDto]): ObjetoExternalDto = {
    // Validamos si el evento debe procesarse antes de continuar
    // Un evento debe descartarse si:
    // 1. Es un evento de semáforo sin vínculo existente, o
    // 2. Es un state parcial sin vínculo existente
    val debeDescartar = (SPO.esEventoSemaforo(evento) || SPO.esStateParcial(evento)) && estado.isEmpty

    if (debeDescartar) {
      // Si debe descartarse, retornar el evento original sin cambios
      // Este será descartado en niveles superiores
      evento
    } else {
      // Caso contrario, aplicar la lógica normal de state parcial
      SPO.stateParcialCC(evento, estado).asInstanceOf[ObjetoExternalDto]
    }
  }

  /**
   * Determina si un evento debe ser procesado o descartado
   *
   * @param evento Evento a validar
   * @param estado Estado previo (opcional)
   * @return true si debe procesarse, false si debe descartarse
   */
  def debeProceserEvento(evento: ObjetoExternalDto, estado: Option[ObjetoExternalDto]): Boolean = {
    // Si no es un evento de semáforo ni un state parcial, siempre procesar
    if (!SPO.esEventoSemaforo(evento) && !SPO.esStateParcial(evento)) {
      return true
    }

    // Si es evento de semáforo o state parcial, solo procesar si existe estado previo
    estado.isDefined
  }
}