package consumers.no_registral.obligacion.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{ObligacionRemove, ObligacionUpdateFromDto}
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.ObligacionesTri
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import timescaledb.TimescaledbNifiToKafka.connOracleNifi

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class ObligacionTributariaTransaction2(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesTri](monitoring) {
  private val log = LoggerFactory.getLogger(this.getClass)

  /** Handles the deserialization of detalles de obligaciones tributarias */
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  def topic = "DGR-COP-OBLIGACIONES-TRI2"

  def topicRetry = "DGR-COP-OBLIGACIONES-TRI2_retry"

  def topicError = "DGR-COP-OBLIGACIONES-TRI2_error"

  def processInput(input: String): Either[Throwable, ObligacionesTri] = {
    if (enable.equals("true")) {
      Future(connOracleNifi(input, "DGR-COP-OBLIGACIONES-TRI")).onComplete {
        case Failure(exception) => log.error("ERROR Future(connOracleNifi(obligacion.EV_ID.toString())) -> " + exception)
        case Success(value) => log.debug("Exito ")
      }
    }
    decode[ObligacionesTri](input)
  }

  def processMessage(obligacion: ObligacionesTri): Future[Response.SuccessProcessing] = {

    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))

    val isCancelada = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES map {
      d => d.RULE_NUMBER.contains("-1")
    }

    val isNotDeuda = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES map {
      d => d.RULE_NUMBER.contains("-2")
    }

    val command: ObligacionCommands =
      if (isCancelada.head)
        ObligacionRemove(
          deliveryId = obligacion.EV_ID,
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          registro = obligacion,
          cuota = obligacion.BOB_CUOTA
        )
      else if (isNotDeuda.head)
        ObligacionRemove(
          deliveryId = obligacion.EV_ID,
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          registro = obligacion,
          cuota = None
        )
      else
        ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = obligacion,
          detallesObligacion = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES,
          isAdheridoDebito = isAdheridoDebito
        )
    actorRef.ask[Response.SuccessProcessing](command)

  }
}
