package consumers.registral.declaracion_jurada.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.declaracion_jurada.application.entities.{DeclaracionJurada, DeclaracionJuradaCommands}
import consumers.registral.declaracion_jurada.infrastructure.dependency_injection.DeclaracionJuradaActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import consumers.registral.declaracion_jurada.infrastructure.json.json._
import monitoring.Monitoring

import scala.concurrent.Future

case class DeclaracionJuradaTransaction(actor: DeclaracionJuradaActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[DeclaracionJurada](monitoring) {
  def topic = "DGR-COP-DECJURADAS"
  def topicRetry = "DGR-COP-DECJURADAS_retry"
  def topicError = "DGR-COP-DECJURADAS_error"

  def processInput(input: String): Either[Throwable, DeclaracionJurada] = {
    println("CUMBIA DECLARACION JURADA " + decode[DeclaracionJurada](input))
    decode[DeclaracionJurada](input)
  }

  override def processMessage(registro: DeclaracionJurada): Future[Response.SuccessProcessing] = {
    val command = DeclaracionJuradaCommands.DeclaracionJuradaUpdateFromDto(
      sujetoId = registro.BDJ_SUJ_IDENTIFICADOR,
      objetoId = registro.BDJ_SOJ_IDENTIFICADOR,
      tipoObjeto = registro.BDJ_SOJ_TIPO_OBJETO,
      declaracionJuradaId = registro.BDJ_DDJ_ID,
      deliveryId = BigInt(registro.EV_ID),
      registro = registro
    )

    actor.ask(command)
  }

}
