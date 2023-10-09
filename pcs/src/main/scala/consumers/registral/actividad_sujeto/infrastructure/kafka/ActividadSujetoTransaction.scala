package consumers.registral.actividad_sujeto.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.actividad_sujeto.application.entities.ActividadSujeto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser._
import monitoring.Monitoring
import scala.concurrent.Future
import consumers.registral.actividad_sujeto.infrastructure.json.json._

case class ActividadSujetoTransaction(actor: ActividadSujetoActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ActividadSujeto](monitoring) {
  def topic = "DGR-COP-ACTIVIDADES"
  def topicRetry = "DGR-COP-ACTIVIDADES_retry"
  def topicError = "DGR-COP-ACTIVIDADES_error"


  def processInput(input: String): Either[Throwable, ActividadSujeto] = {
    decode[ActividadSujeto](input)
  }

  override def processMessage(registro: ActividadSujeto): Future[Response.SuccessProcessing] = {
    val command =
      ActividadSujetoUpdateFromDto(
        sujetoId = registro.BAT_SUJ_IDENTIFICADOR,
        actividadSujetoId = registro.BAT_ATD_ID,
        deliveryId = BigInt(registro.EV_ID),
        registro = registro
      )

    actor.ask(command)
  }
}
