package consumers.no_registral.sujeto.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future


case class SujetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[SujetoTri](monitoring) {

  def topic = "DGR-COP-SUJETO-TRI"
  def topicRetry = "DGR-COP-SUJETO-TRI_retry"
  def topicError = "DGR-COP-SUJETO-TRI_error"
  def processInput(input: String): Either[Throwable, SujetoTri] =
    decode[SujetoTri](input)

  def processMessage(registro: SujetoTri): Future[Response.SuccessProcessing] = {
    //connOracleKafkaToWriteside(registro.EV_ID.toString(), "sujeto", registro.SUJ_CANAL_ORIGEN.getOrElse("TAX"))

    if (registro.SUJ_IDENTIFICADOR == "") {
      Future.failed(new IllegalArgumentException("Campos obligatorios vacíos, operación omitida"))
    } else {
      val command = registro match {
        case _: SujetoTri =>
          SujetoCommands.SujetoUpdateFromTri(
            sujetoId = registro.SUJ_IDENTIFICADOR,
            deliveryId = registro.EV_ID,
            registro = registro
          )
      }
      actorRef.ask[Response.SuccessProcessing](command)
    }
  }
}
