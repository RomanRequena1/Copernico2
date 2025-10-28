package consumers.no_registral.obligacion.application.helper

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, DetallesObligacionCaracteristicas, DetallesSupresiones, ListCaracteristicasObligaciones, ListDetallesObligaciones, ListDetallesSupresiones, ObligacionExternalDto}
import cqrs.untyped.command.StateParcial

import java.lang.reflect.Field

class StateParcialObligacion extends StateParcial {
  /**
   * Casos de otros atributos para Obligacion:
   * 1- Evento con oAtri en None, sin estado
   * 2- Evento con oAtri en None, con estado (Independiente del valor de oAtri en estado)
   * 3- Evento con oAtri con valores, sin estado
   * 4- Evento con oAtri con valores, con estado
   * 4.1- Con estado oAtri en None
   * 4.2- Con estado oAtri con valores
   *
   * Casos de supresiones para Obligacion:
   * 1-
   * @param evento
   * @param estado
   * @param campoActualizar
   * @param eventoNuevo
   */
  override def spOtrosAtributos(evento: Object, estado: Option[Object], campoActualizar: Field, eventoNuevo: Object): Unit = {

    val miEvento: ObligacionExternalDto = evento match {
      case e: ObligacionExternalDto => e
    }

    campoActualizar.getName match {
      case "BOB_OTROS_ATRIBUTOS" => {

        miEvento.BOB_OTROS_ATRIBUTOS match {
          case None =>
            estado match {
              // Caso 1- Evento con oAtri en None, sin estado
              case None => ()
              // Caso 2- Evento con oAtri en None, con estado (Independiente del valor de oAtri en estado)
              case Some(oEstado: ObligacionExternalDto) => campoActualizar.set(eventoNuevo, oEstado.BOB_OTROS_ATRIBUTOS)
              case _ => ()
            }
          case Some(oAtriEvento) =>
            estado match {
              // Caso 3- Evento con oAtri con valores, sin estado
              case None => {
                val otrosAtributosEventoDetalles = oAtriEvento.BOB_DETALLES.head
                // Validar campos en defaults para evento
                val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEventoDetalles, None)
                campoActualizar.set(eventoNuevo, Some(ListDetallesObligaciones(List(otrosAtributosUpdated match { case oau: DetallesObligacion => oau }))))
              }
              // Caso 4- Evento con oAtri con valores, con estado
              case Some(oEstado: ObligacionExternalDto) =>
                oEstado.BOB_OTROS_ATRIBUTOS match {
                  // Caso 4.1- Con estado oAtri en None
                  case None => {
                    val otrosAtributosEventoDetalles = oAtriEvento.BOB_DETALLES.head
                    val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEventoDetalles, None)
                    campoActualizar.set(eventoNuevo, Some(ListDetallesObligaciones(List(otrosAtributosUpdated match { case oau: DetallesObligacion => oau }))))
                  }
                  // Caso 4.2- Con estado oAtri con valores
                  case Some(oAtriEstado) => {
                    val otrosAtributosEvento = miEvento.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head
                    val otrosAtributosEstado = oAtriEstado.BOB_DETALLES.head
                    val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEvento, Some(otrosAtributosEstado))
                    campoActualizar.set(eventoNuevo, Some(ListDetallesObligaciones(List(otrosAtributosUpdated match { case oau: DetallesObligacion => oau }))))
                  }
                }
              case _ => ()
            }
        }

      }

      case "BOB_CARACTERISTICAS" => {
        estado match {
          case Some(oEstado: ObligacionExternalDto) if oEstado.BOB_CARACTERISTICAS.isDefined => {
            if (miEvento.BOB_CARACTERISTICAS.get.BOB_DETALLES_CARACTERISTICAS.isEmpty) {
              campoActualizar.set(eventoNuevo, None)
            } else {
              campoActualizar.set(eventoNuevo, miEvento.BOB_CARACTERISTICAS)
            }
          }
          case _ => ()
        }
      }

      case "BOB_SUPRESIONES" => {
        estado match {
          case Some(oEstado: ObligacionExternalDto) if oEstado.BOB_SUPRESIONES.isDefined => {
            if (miEvento.BOB_SUPRESIONES.get.BOB_DETALLES_SUPRESIONES.isEmpty) {
              campoActualizar.set(eventoNuevo, None)
            } else {
              campoActualizar.set(eventoNuevo, miEvento.BOB_SUPRESIONES)
            }
          }
          case _ => ()
        }
      }
    }

  }

}

object StateParcialObligacion {
  val SPO = new StateParcialObligacion()
  def stateParcialCC(evento: ObligacionExternalDto, estado: Option[ObligacionExternalDto]): ObligacionExternalDto = {
    SPO.stateParcialCC(evento, estado) match {case o: ObligacionExternalDto => o}
  }
}