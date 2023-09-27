package consumers.registral.juicio_tri.application.entities

import consumers.registral.juicio_tri.application.entities.JuicioDosMessage.JuicioDosMessageRoots
import design_principles.actor_model.ShardedMessage


trait JuicioDosMessage extends ShardedMessage {

  val juicioId: String

  override def aggregateRoot: String =
    JuicioDosMessageRoots(
      juicioId
    ).toString
}
object JuicioDosMessage {
  case class JuicioDosMessageRoots(juicioId: String) {
    override def toString = s"Juicio-$juicioId"
  }
  object JuicioDosMessageRoots {
    def extractor(persistenceId: String): JuicioDosMessageRoots =
      persistenceId match {
        case s"Juicio-$juicioId" =>
          JuicioDosMessageRoots(juicioId)
      }
  }
}
