package consumers.registral.exclusiones_sujeto.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.exclusiones_sujeto.application.entities.{ExclusionesSujetoCommands, ExclusionesSujetoTri}
import consumers.registral.exclusiones_sujeto.infrastructure.dependency_injection.ExclusionesSujetoActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import io.circe.parser._
import consumers.registral.exclusiones_sujeto.infrastructure.json._
import scala.concurrent.Future


case class ExclusionesSujetoTributarioTransaction (actor: ExclusionesSujetoActor, monitoring: Monitoring)(
implicit
actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ExclusionesSujetoTri](monitoring) {
  def topic = "DGR-COP-ETAPROCESALES-TRI"
  def topicRetry = "DGR-COP-ETAPROCESALES-TRI_retry"
  def topicError = "DGR-COP-ETAPROCESALES-TRI_error"

  def processInput(input: String): Either[Throwable, ExclusionesSujetoTri] = {
    decode[ExclusionesSujetoTri](input)
  }



  override def processMessage(registro: ExclusionesSujetoTri): Future[Response.SuccessProcessing] = {
    val command = ExclusionesSujetoCommands.ExclusionesSujetoUpdateFromDto(
      sujetoId = registro.BSE_SUJ_IDENTIFICADOR,
      deliveryId = registro.EV_ID,
      registro = registro
    )
    actor.ask(command)
  }
}
