package consumers.no_registral.objeto.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateCotitulares
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future
case class ObjetoUpdateCotitularesTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoUpdateCotitulares](monitoring) {

  def topic = "ObjetoUpdatedCotitulares"
  def topicRetry = "ObjetoUpdatedCotitulares_retry"
  def topicError = "ObjetoUpdatedCotitulares_error"

  def processInput(input: String): Either[Throwable, ObjetoUpdateCotitulares] =
    decode[ObjetoUpdateCotitulares](input)

  def processMessage(cmd: ObjetoUpdateCotitulares): Future[Response.SuccessProcessing] = {
    println("ObjetoUpdateCotitularesTransaction")
    actorRef.ask[Response.SuccessProcessing](cmd)
  }
}
