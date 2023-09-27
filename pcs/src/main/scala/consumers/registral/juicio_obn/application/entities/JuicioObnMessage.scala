package consumers.registral.juicio_obn.application.entities

import consumers.registral.juicio_obn.application.entities.JuicioObnMessage.JuicioObnMessageRoot
import design_principles.actor_model.ShardedMessage

trait JuicioObnMessage extends ShardedMessage{
  val juicioObnId: String

  val tipoObjeto: String

  val objetoId: String

  val obligacionId: String



  override def  aggregateRoot: String = {
    JuicioObnMessageRoot(
      juicioObnId,
      tipoObjeto,
      objetoId,
      obligacionId
    )
  }.toString

}

object JuicioObnMessage {
  case class JuicioObnMessageRoot(juicioObnId: String, tipoObjeto: String, objetoId: String,obligacionId: String ){
    override def toString: String = s"JuicioObn-$juicioObnId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId"
  }
}
