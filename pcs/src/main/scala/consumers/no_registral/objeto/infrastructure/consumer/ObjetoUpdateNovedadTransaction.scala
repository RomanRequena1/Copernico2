package consumers.no_registral.objeto.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoSnapshot
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future
case class ObjetoUpdateNovedadTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoSnapshot](monitoring) {

  def topic = "ObjetoReceiveSnapshot"
  def topicRetry = "ObjetoReceiveSnapshot_retry"
  def topicError = "ObjetoReceiveSnapshot_error"

  def processInput(input: String): Either[Throwable, ObjetoSnapshot] =
    decode[ObjetoSnapshot](input)

  def processMessage(cmd: ObjetoSnapshot): Future[Response.SuccessProcessing] = {
    println("ObjetoUpdateNovedadTransaction")
    actorRef.ask[Response.SuccessProcessing](cmd)
  }

}
