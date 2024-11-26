package cqrs.untyped.command

import ddd.ExternalDto

import java.lang.reflect.Field
import java.time.LocalDateTime

abstract class StateParcial {

  def spOtrosAtributos(evento: Object, estado: Option[Object], campoActualizar: Field, eventoNuevo: Object): Unit

  final def stateParcialCC(evento: Object, estado: Option[Object]): Object = {
    val declaredFields = evento.getClass.getDeclaredFields
    var objetoNuevoTest = evento

    declaredFields.foreach { campo =>
      val campoEvento = objetoNuevoTest.getClass.getDeclaredField(campo.getName)
      campoEvento.setAccessible(true)
      campoEvento match {
        case cEv if (cEv.get(evento) == None && estado.isDefined) => {
          val campoEstado = estado.get.getClass.getDeclaredField(campo.getName)
          campoEstado.setAccessible(true)
          campoEvento.set(objetoNuevoTest, campoEstado.get(estado.get))
        }
        case cEv if (
            cEv.get(evento).equals(Some("null")) ||
            cEv.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0))) ||
            cEv.get(evento).equals(Some(999))
          ) => campoEvento.set(objetoNuevoTest, None)
        case cEv if (campoEvento.getName == "SOJ_OTROS_ATRIBUTOS" ||
          campoEvento.getName == "BOB_OTROS_ATRIBUTOS") ||
          campoEvento.getName == "BOB_SUPRESIONES" => spOtrosAtributos(evento, estado, cEv, objetoNuevoTest)
        case _ => ()
      }
    }
    objetoNuevoTest
  }

}
