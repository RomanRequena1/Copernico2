package consumers.registral.objeto_juicio.application.entities

import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioMessage.ObjetoJuicioMessageRoots
import design_principles.actor_model.ShardedMessage

trait ObjetoJuicioMessage extends ShardedMessage {

  val objetoId: String
  val tipoObjeto: String
  val juicioId: String
  val planId: String

  override def aggregateRoot: String =
    ObjetoJuicioMessageRoots(
      objetoId,
      tipoObjeto,
      juicioId,
      planId
    ).toString
}

object ObjetoJuicioMessage {

  case class ObjetoJuicioMessageRoots(objetoId: String, tipoObjeto: String, juicioId: String, planId: String) {
    override def toString = s"Objeto-$objetoId-$tipoObjeto-Juicio-$juicioId-Plan-$planId"
  }
  object ObjetoJuicioMessageRoots {

    def extractor(persistenceId: String): ObjetoJuicioMessageRoots =
      persistenceId match {
        case s"Objeto-$objetoId-$tipoObjeto-Juicio-$juicioId-Plan-$planId" =>
          ObjetoJuicioMessageRoots(objetoId, tipoObjeto, juicioId, planId)
      }
  }
}
