package consumers.no_registral.objeto.application.helper

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import cqrs.untyped.command.StateParcial

import java.lang.reflect.Field
import java.time.LocalDateTime

class StateParcialObjeto extends StateParcial {


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

//    estado match {
//      case None => {
//        val otrosAtributosEvento = miEvento.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head
//        val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEvento, None)
//        campoActualizar.set(eventoNuevo, Some(ListDetallesObjeto(List(otrosAtributosUpdated.asInstanceOf[DetallesObjeto]))))
//      }
//
//      case Some(e: ObjetoExternalDto) => {
//        e.SOJ_OTROS_ATRIBUTOS match {
//          case None => ()
//          case Some(value) if value.SOJ_DETALLES.nonEmpty => {
//            val otrosAtributosEvento = miEvento.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head
//            val otrosAtributosEstado = value.SOJ_DETALLES.head
//            val otrosAtributosUpdated = super.stateParcialCC(otrosAtributosEvento, Some(otrosAtributosEstado))
//            campoActualizar.set(eventoNuevo, Some(ListDetallesObjeto(List(otrosAtributosUpdated.asInstanceOf[DetallesObjeto]))))
//          }
//        }
//      }
//      case _ => ()
//    }


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
    SPO.stateParcialCC(evento, estado).asInstanceOf[ObjetoExternalDto]
  }
}
