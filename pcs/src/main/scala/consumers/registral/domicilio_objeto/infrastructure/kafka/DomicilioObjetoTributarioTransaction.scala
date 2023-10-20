package consumers.registral.domicilio_objeto.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoExternalDto.DomicilioObjetoTri
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import monitoring.Monitoring
import consumers.registral.domicilio_objeto.infrastructure.json._

import scala.concurrent.Future

case class DomicilioObjetoTributarioTransaction(actor: DomicilioObjetoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DomicilioObjetoTri](monitoring) {
  def topic = "DGR-COP-DOMICILIO-OBJ-TRI"
  def topicRetry = "DGR-COP-DOMICILIO-OBJ-TRI_retry"
  def topicError = "DGR-COP-DOMICILIO-OBJ-TRI_error"

  def processInput(input: String): Either[Throwable, DomicilioObjetoTri] =
    decode[DomicilioObjetoTri](input)

  override def processMessage(registro: DomicilioObjetoTri): Future[Response.SuccessProcessing] = {
    val command = DomicilioObjetoCommands.DomicilioObjetoUpdateFromDto(
      sujetoId = registro.BDO_SUJ_IDENTIFICADOR,
      objetoId = registro.BDO_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BDO_SOJ_TIPO_OBJETO,
      domicilioId = registro.BDO_DOM_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor.ask(command)
  }

}
