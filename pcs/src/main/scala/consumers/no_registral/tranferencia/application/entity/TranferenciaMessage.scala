package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.tranferencia.application.entity.TranferenciaMessage.TranferenciaMessageRoots
import design_principles.actor_model.ShardedMessage

trait TranferenciaMessage extends ShardedMessage {
  def objetoId: String

  override def entityId: String = objetoId
  override def shardedId: String = objetoId

  override def aggregateRoot: String =
    TranferenciaMessageRoots(
      objetoId
    ).toString
}
object TranferenciaMessage {

  case class TranferenciaMessageRoots(objetoId: String) {
    override def toString: String = s"ObjetoTranf-$objetoId"
  }

  object TranferenciaMessageRoots {
    def extractor(persistenceId: String): TranferenciaMessageRoots =
      persistenceId match {
        case s"ObjetoTranf-$objetoId" =>
          TranferenciaMessageRoots(objetoId)
        case s"$objetoId" =>
          TranferenciaMessageRoots(objetoId)
      }
  }
}
