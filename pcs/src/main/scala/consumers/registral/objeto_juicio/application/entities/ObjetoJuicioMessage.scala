package consumers.registral.objeto_juicio.application.entities

import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioMessage.ObjetoJuicioMessageRoots
import design_principles.actor_model.ShardedMessage

trait ObjetoJuicioMessage extends ShardedMessage {

  val objetoId: String
  val tipoObjeto: String
  val idRel: String
  val tipoObjetoRel: String

  override def aggregateRoot: String =
    ObjetoJuicioMessageRoots(
      objetoId,
      tipoObjeto,
      idRel,
      tipoObjetoRel
    ).toString
}

object ObjetoJuicioMessage {

  case class ObjetoJuicioMessageRoots(objetoId: String, tipoObjeto: String, idRel: String, tipoObjetoRel: String) {
    override def toString = s"Objeto-$objetoId-TipoObjeto-$tipoObjeto-IdRelacion-$idRel-TipoObjetoRel-$tipoObjetoRel"
  }
  object ObjetoJuicioMessageRoots {

    def extractor(persistenceId: String): ObjetoJuicioMessageRoots =
      persistenceId match {
        case s"Objeto-$objetoId-TipoObjeto-$tipoObjeto-IdRelacion-$idRel-TipoObjetoRel-$tipoObjetoRel" =>
          ObjetoJuicioMessageRoots(objetoId, tipoObjeto, idRel, tipoObjetoRel)
      }
  }
}
