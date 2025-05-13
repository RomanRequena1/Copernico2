package consumers.registral.objeto_juicio.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.ObjetoJuicioTri
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import consumers.registral.objeto_juicio.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import monitoring.Monitoring
import org.slf4j.LoggerFactory

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

    val command: ObjetoJuicioCommands = {
      if(registro.RJP_ESTADO.contains("BAJA")) {
        ObjetoJuicioCommands.RemoveObjetoJuicioFromDto(
          objetoId = registro.RJP_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.RJP_SOJ_TIPO_OBJETO,
          idRel = registro.RJP_IDENTIFICADOR_REL,
          tipoObjetoRel = registro.RJP_TIPO_OBJETO_REL,
          tipoRel = registro.RJP_TIPO_REL,
          idExterno = registro.RJP_ID_EXTERNO,
          idExterno2 = registro.RJP_ID_EXTERNO_2,
          estado = registro.RJP_ESTADO,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
      } else {
        ObjetoJuicioCommands.ObjetoJuicioUpdateFromDto(
          objetoId = registro.RJP_SOJ_IDENTIFICADOR,
          tipoObjeto = registro.RJP_SOJ_TIPO_OBJETO,
          idRel = registro.RJP_IDENTIFICADOR_REL,
          tipoObjetoRel = registro.RJP_TIPO_OBJETO_REL,
          tipoRel = registro.RJP_TIPO_REL,
          idExterno = registro.RJP_ID_EXTERNO,
          idExterno2 = registro.RJP_ID_EXTERNO_2,
          estado = registro.RJP_ESTADO,
          deliveryId = BigInt(registro.EV_ID),
          registro = registro
        )
      }
    }
    actor.ask(command)
  }
}
