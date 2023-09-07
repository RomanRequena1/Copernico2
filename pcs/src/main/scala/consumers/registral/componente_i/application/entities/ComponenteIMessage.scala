package consumers.registral.componente_i.application.entities

import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.sujeto.application.entity.SujetoMessage
import consumers.registral.componente_i.application.entities.ComponenteIMessage.ComponenteIMessageRoots
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoMessage.CuponDescuentoMessageRoots

trait ComponenteIMessage extends design_principles.actor_model.ShardedMessage with SujetoMessage with ObjetoMessage {
  def obligacionId: String
  override def aggregateRoot: String =
    ComponenteIMessageRoots(
      sujetoId,
      objetoId,
      tipoObjeto,
      obligacionId
    ).toString
}

object ComponenteIMessage {

  case class ComponenteIMessageRoots(sujetoId: String, objetoId: String, tipoObjeto: String, obligacionId: String) {
    override def toString = s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId-componente_i"
  }
  object ComponenteIMessageRoots {

    def extractor(persistenceId: String): ComponenteIMessageRoots =
      persistenceId match {
        case s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId-componente_i" =>
          ComponenteIMessageRoots(sujetoId, objetoId, tipoObjeto, obligacionId)
      }
  }
}
