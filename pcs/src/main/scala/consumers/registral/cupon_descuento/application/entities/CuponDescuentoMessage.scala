package consumers.registral.cupon_descuento.application.entities

import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.sujeto.application.entity.SujetoMessage
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoMessage.CuponDescuentoMessageRoots

trait CuponDescuentoMessage extends design_principles.actor_model.ShardedMessage with SujetoMessage with ObjetoMessage {
  def obligacionId: String
  override def aggregateRoot: String =
    CuponDescuentoMessageRoots(
      sujetoId,
      objetoId,
      tipoObjeto,
      obligacionId
    ).toString
}

object CuponDescuentoMessage {

  case class CuponDescuentoMessageRoots(sujetoId: String, objetoId: String, tipoObjeto: String, obligacionId: String) {
    override def toString = s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId-cupon-descuento"
  }
  object CuponDescuentoMessageRoots {

    def extractor(persistenceId: String): CuponDescuentoMessageRoots =
      persistenceId match {
        case s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId-cupon-descuento" =>
          CuponDescuentoMessageRoots(sujetoId, objetoId, tipoObjeto, obligacionId)
      }
  }
}
