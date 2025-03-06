package consumers.no_registral.sujeto.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoAnt
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future

case class SujetoNoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[SujetoAnt](monitoring) {

  def topic = "DGR-COP-SUJETO-ANT"

  def topicRetry = "DGR-COP-SUJETO-ANT_retry"

  def topicError = "DGR-COP-SUJETO-ANT_error"

  def processInput(input: String): Either[Throwable, SujetoAnt] = {
    decode[SujetoAnt](input)
  }

  def processMessage(registro: SujetoAnt): Future[Response.SuccessProcessing] = {
    if (registro.SUJ_IDENTIFICADOR == "") {
      Future.successful(Response.SuccessProcessing("Campos obligatorios vacíos, operación omitida", registro.EV_ID))
    } else {
      val command = registro match {
        case _: SujetoAnt =>
          SujetoCommands.SujetoUpdateFromAnt(
            sujetoId = registro.SUJ_IDENTIFICADOR,
            deliveryId = registro.EV_ID,
            registro = registro
          )
      }
      actorRef.ask[Response.SuccessProcessing](command)
    }
  }
}

