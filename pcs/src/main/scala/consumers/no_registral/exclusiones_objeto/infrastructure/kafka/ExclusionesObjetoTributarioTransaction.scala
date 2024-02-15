package consumers.no_registral.exclusiones_objeto.infrastructure.kafka

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.exclusiones_objeto.application.entities.{ExclusionesObjetoCommands, ExclusionesObjetoTri}
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring
import consumers.no_registral.exclusiones_objeto.infrastructure.json.ExclusionesObjetoImplicits._

import scala.concurrent.Future

case class ExclusionesObjetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ExclusionesObjetoTri](monitoring) {
  def topic = "DGR-COP-EXCLUSIONES-OBJETO-TRI"

  def topicRetry = "DGR-COP-EXCLUSIONES-OBJETO-TRI_retry"

  def topicError = "DGR-COP-EXCLUSIONES-OBJETO-TRI_error"

  def processInput(input: String): Either[Throwable, ExclusionesObjetoTri] = {
    decode[ExclusionesObjetoTri](input)
  }


  override def processMessage(registro: ExclusionesObjetoTri): Future[Response.SuccessProcessing] = {
    val command = ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto(
      objetoId = registro.BOE_SOJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actorRef.ask[Response.SuccessProcessing](command)
  }
}

