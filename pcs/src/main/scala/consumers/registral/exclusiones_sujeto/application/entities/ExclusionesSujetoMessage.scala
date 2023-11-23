package consumers.registral.exclusiones_sujeto.application.entities

import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoMessage.ExclusionesSujetoMessageRoots
import design_principles.actor_model.ShardedMessage

trait ExclusionesSujetoMessage extends ShardedMessage {
  def sujetoId: String

  override def aggregateRoot: String =
    ExclusionesSujetoMessageRoots(
      sujetoId
    ).toString
}
object ExclusionesSujetoMessage {

  case class ExclusionesSujetoMessageRoots(sujetoId: String) {
    override def toString = s"Sujeto-$sujetoId-ExclusionSujeto"
  }
  object ExclusionesSujetoMessageRoots {

    def extractor(persistenceId: String): ExclusionesSujetoMessageRoots =
      persistenceId match {
        case s"Sujeto-$sujetoId" =>
          ExclusionesSujetoMessageRoots(sujetoId)
      }
  }
}
