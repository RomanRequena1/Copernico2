package consumers.registral.exclusiones_objeto.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.exclusiones_objeto.application.entities.{ExclusionesObjetoCommands, ExclusionesObjetoTri}
import consumers.registral.exclusiones_objeto.infrastructure.dependency_injection.ExclusionesObjetoActor
import consumers.registral.exclusiones_objeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import monitoring.Monitoring
import scala.concurrent.Future


case class ExclusionesObjetoTributarioTransaction(actor: ExclusionesObjetoActor, monitoring: Monitoring)(
implicit
actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ExclusionesObjetoTri](monitoring) {
  def topic = "DGR-COP-EXCLUSIONES-OBJETO-TRI"

  def topicRetry = "DGR-COP-EXCLUSIONES-OBJETO-TRI_retry"

  def topicError = "DGR-COP-EXCLUSIONES-OBJETO-TRI_error"

  def processInput(input: String): Either[Throwable, ExclusionesObjetoTri] = {
    println("ROMAN WRITESIDE: " + decode[ExclusionesObjetoTri](input))
    decode[ExclusionesObjetoTri](input)
}


  override def processMessage(registro: ExclusionesObjetoTri): Future[Response.SuccessProcessing] = {
    val command = ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto(
      objetoId = registro.BOE_SOJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actor.ask(command)
  }
}
