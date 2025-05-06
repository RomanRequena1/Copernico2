package consumers.registral.objeto_juicio.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.{ObjetoJuicioAnt, ObjetoJuicioTri}
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import consumers.registral.objeto_juicio.infrastructure.json._

import scala.concurrent.Future

case class ObjetoJuicioTributarioTransaction(actor: ObjetoJuicioActor, monitoring: Monitoring)(

  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoJuicioTri](monitoring) {

  private val log = LoggerFactory.getLogger(this.getClass)
  def topic = "DGR-COP-OBJETOJUICIOS-TRI"
  def topicRetry = "DGR-COP-OBJETOJUICIOS-TRI_retry"
  def topicError = "DGR-COP-OBJETOJUICIOS-TRI_error"

  def processInput(input: String): Either[Throwable, ObjetoJuicioTri] = {
    decode[ObjetoJuicioTri](input)
  }

  override def processMessage(registro: ObjetoJuicioTri): Future[Response.SuccessProcessing] = {

    val command: ObjetoJuicioCommands.ObjetoJuicioUpdateFromDto =
      ObjetoJuicioCommands.ObjetoJuicioUpdateFromDto(
        objetoId = registro.OJU_SOJ_IDENTIFICADOR,
        tipoObjeto = registro.OJU_SOJ_TIPO_OBJETO,
        juicioId = registro.OJU_JUI_ID,
        planId = registro.OJU_PLAN_ID,
        deliveryId = BigInt(registro.EV_ID),
        registro = registro
      )
    actor.ask(command)
  }
}

