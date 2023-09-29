package consumers.registral.etapas_procesales.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.etapas_procesales.application.entities.{EtapasProcesalesCommands, EtapasProcesalesTri}
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import io.circe.parser._

import scala.concurrent.Future
import consumers.registral.etapas_procesales.infrastructure.json._

case class EtapasProcesalesTributarioTransaction(actor: EtapasProcesalesActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[EtapasProcesalesTri](monitoring) {
  def topic = "DGR-COP-ETAPROCESALES-TRI"
  def topicRetry = "DGR-COP-ETAPROCESALES-TRI_retry"
  def topicError = "DGR-COP-ETAPROCESALES-TRI_error"

  def processInput(input: String): Either[Throwable, EtapasProcesalesTri] =
    decode[EtapasProcesalesTri](input)

  override def processMessage(registro: EtapasProcesalesTri): Future[Response.SuccessProcessing] = {
    val command = EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto(
      juicioId = registro.BEP_JUI_ID,
      etapaId = registro.BPE_ETA_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )
    actor.ask(command)
  }
}
