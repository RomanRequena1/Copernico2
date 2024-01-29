package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage.ObjetoVinculoMessageRoots
import design_principles.actor_model.ShardedMessage

trait ObjetoVinculoMessage extends ShardedMessage {
  def objetoId: String

  override def entityId: String = objetoId
  override def shardedId: String = objetoId

  override def aggregateRoot: String =
    ObjetoVinculoMessageRoots(
      objetoId
    ).toString
}
object ObjetoVinculoMessage {

  case class ObjetoVinculoMessageRoots(objetoId: String) {
    override def toString: String = s"ObjetoVinculo-$objetoId"
  }

  object ObjetoVinculoMessageRoots {
    def extractor(persistenceId: String): ObjetoVinculoMessageRoots =
      persistenceId match {
        case s"ObjetoVinculo-$objetoId" =>
          ObjetoVinculoMessageRoots(objetoId)
        case s"$objetoId" =>
          ObjetoVinculoMessageRoots(objetoId)
      }
  }
}
