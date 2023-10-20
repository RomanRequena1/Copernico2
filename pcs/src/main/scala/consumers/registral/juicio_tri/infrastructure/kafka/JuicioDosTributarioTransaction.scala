package consumers.registral.juicio_tri.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.juicio_tri.application.entities.JuicioDosCommands.{JuicioDosRemoveFromDto, JuicioDosUpdateFromDto}
import consumers.registral.juicio_tri.application.entities.JuicioDosTri
import consumers.registral.juicio_tri.infrastructure.dependency_injection.JuicioDosActor
import consumers.registral.juicio_tri.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import io.circe.parser.decode
import scala.concurrent.Future

case class JuicioDosTributarioTransaction(actor: JuicioDosActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
)extends ActorTransaction[JuicioDosTri](monitoring) {
  def topic = "DGR-COP-JUICIOS-CAB-TRI"

  def topicRetry = "DGR-COP-JUICIOS-CAB-TRI_retry"

  def topicError = "DGR-COP-JUICIOS-CAB-TRI_error"

  def processInput(input: String): Either[Throwable, JuicioDosTri] = {
    decode[JuicioDosTri](input)
  }

  override def processMessage(registro: JuicioDosTri): Future[Response.SuccessProcessing] = {
    val command = registro.BJU_ESTADO match {
      case x if x.contains("BAJA") =>
        JuicioDosRemoveFromDto(
          juicioId = registro.BJU_IDENTIFICADOR,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
      case _ =>
        JuicioDosUpdateFromDto(
          juicioId = registro.BJU_IDENTIFICADOR,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
    }
    actor.ask(command)
  }
}
