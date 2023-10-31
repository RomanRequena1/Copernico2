package consumers.registral.juicio_obn.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.{JuicioObnDeleteFromDto, JuicioObnUpdateFromDto}
import consumers.registral.juicio_obn.application.entities.{DetallesJuicioTri, JuicioObnCommands, JuicioObnTri}
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import design_principles.actor_model.Response
import monitoring.Monitoring
import consumers.registral.juicio_obn.infrastructure.json.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._

import scala.concurrent.Future

case class JuicioObnTributarioTransaction(actor: JuicioObnActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[JuicioObnTri](monitoring) {
  override def topic: String = "DGR-COP-JUICIOS-OBLIGACIONES-TRI"

  override def topicRetry: String = "DGR-COP-JUICIOS-OBLIGACIONES-TRI_retry"

  override def topicError: String = "DGR-COP-JUICIOS-OBLIGACIONES-TRI_error"

  override def processInput(input: String): Either[Throwable, JuicioObnTri] = {
    decode[JuicioObnTri](input)
  }


  override def processMessage(juicioObn: JuicioObnTri): Future[Response.SuccessProcessing] = {

    val isNotDeuda: List[Boolean] = juicioObn.BJD_OTROS_ATRIBUTOS.get.BJD_DETALLES map {
      d => d.RULE_NUMBER.contains("-1")
    }

    val command: JuicioObnCommands =
      if (isNotDeuda.head) {
        JuicioObnCommands.JuicioObnDeleteFromDto(
          deliveryId = juicioObn.EV_ID,
          juicioObnId = juicioObn.BJU_IDENTIFICADOR,
          objetoId = juicioObn.BJD_SOJ_IDENTIFICADOR,
          tipoObjeto = juicioObn.BJD_SOJ_TIPO_OBJETO,
          obligacionId = juicioObn.BJD_OBN_ID,
          registro = juicioObn)
      }
      else {
        JuicioObnUpdateFromDto(
          deliveryId = juicioObn.EV_ID,
          juicioObnId = juicioObn.BJU_IDENTIFICADOR,
          objetoId = juicioObn.BJD_SOJ_IDENTIFICADOR,
          tipoObjeto = juicioObn.BJD_SOJ_TIPO_OBJETO,
          obligacionId = juicioObn.BJD_OBN_ID,
          registro = juicioObn)
      }
    actor.ask(command)
  }

}
