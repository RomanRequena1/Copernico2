package consumers.no_registral.exclusiones_objeto.application.entities

import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoMessage.ExclusionesObjetoMessageRoots
import design_principles.actor_model.ShardedMessage

trait ExclusionesObjetoMessage extends ShardedMessage {
  def objetoId: String

  override def aggregateRoot: String =
    ExclusionesObjetoMessageRoots(
      objetoId
    ).toString
}
object ExclusionesObjetoMessage {

  case class ExclusionesObjetoMessageRoots(objetoId: String) {
    override def toString = s"Objeto-$objetoId-ExclusionObjeto"
  }
  object ExclusionesObjetoMessageRoots {

    def extractor(persistenceId: String): ExclusionesObjetoMessageRoots =
      persistenceId match {
        case s"Objeto-$objetoId" =>
          ExclusionesObjetoMessageRoots(objetoId)
      }
  }
}
