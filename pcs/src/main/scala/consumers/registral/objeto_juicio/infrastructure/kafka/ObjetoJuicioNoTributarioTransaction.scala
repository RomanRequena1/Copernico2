package consumers.registral.objeto_juicio.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.ObjetoJuicioAnt
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import consumers.registral.objeto_juicio.infrastructure.json._
import scala.concurrent.Future

case class ObjetoJuicioNoTributarioTransaction(actor: ObjetoJuicioActor, monitoring: Monitoring)(

  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoJuicioAnt](monitoring) {

  private val log = LoggerFactory.getLogger(this.getClass)
  def topic = "DGR-COP-OBJETOJUICIOS-ANT"
  def topicRetry = "DGR-COP-OBJETOJUICIOS-ANT_retry"
  def topicError = "DGR-COP-OBJETOJUICIOS-ANT_error"

  def processInput(input: String): Either[Throwable, ObjetoJuicioAnt] = {
    decode[ObjetoJuicioAnt](input)
  }

  override def processMessage(registro: ObjetoJuicioAnt): Future[Response.SuccessProcessing] = {

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

