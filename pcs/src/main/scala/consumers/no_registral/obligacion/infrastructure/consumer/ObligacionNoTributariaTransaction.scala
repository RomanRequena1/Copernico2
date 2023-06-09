package consumers.no_registral.obligacion.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoSnapshot
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{ObligacionRemove, ObligacionUpdateFromDto}
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{DetallesObligacion, ObligacionesAnt, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json._
import design_principles.actor_model.Response
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import play.api.libs.json.Reads
import serialization.{decodeF, maybeDecode}
import timescaledb.TimescaledbKafkaToPcs.connOracleKafkaToWriteside
import timescaledb.TimescaledbNifiToKafka.connOracleNifi

import scala.util.{Failure, Success, Try}

case class ObligacionNoTributariaTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesAnt](monitoring) {
  private val log = LoggerFactory.getLogger(this.getClass)
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  def topic = "DGR-COP-OBLIGACIONES-ANT"
  def topicRetry = "DGR-COP-OBLIGACIONES-ANT_retry"
  def topicError = "DGR-COP-OBLIGACIONES-ANT_error"


  def processInput(input: String): Either[Throwable, ObligacionesAnt] = {
    //connOracleNifi(input, "DGR-COP-OBLIGACIONES-ANT")
    if (enable.equals("true")) {
      Future(connOracleNifi(input, "DGR-COP-OBLIGACIONES-TRI")).onComplete {
        case Failure(exception) => log.error("ERROR Future(connOracleNifi(obligacion.EV_ID.toString())) -> " + exception)
        case Success(value) => log.debug("Exito ")
      }
    }
    maybeDecode[ObligacionesAnt](input)
}

  def processMessage(obligacion: ObligacionesAnt): Future[Response.SuccessProcessing] = {

    //log.debug("KW oracle")
    if (enable.equals("true")) {
      Future(connOracleKafkaToWriteside(obligacion.EV_ID.toString())).onComplete {
        case Failure(exception) => log.error("ERROR Future(connOracleKafkaToWriteside(obligacion.EV_ID.toString())) -> " + exception)
        case Success(value) => log.debug("Exito ")
      }
    }
    implicit val b: Reads[Seq[DetallesObligacion]] = Reads.seq(DetallesObligacionF.reads)

    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))

    val detalles: Option[Seq[DetallesObligacion]] = for {
      otrosAtributos <- obligacion.BOB_OTROS_ATRIBUTOS
      bobDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesObligacion]](bobDetalles.toString)
    } yield detalles

    val command = obligacion match {
      case obn: ObligacionesAnt if isNotDeuda(obn) =>
        ObligacionRemove(
          deliveryId = obn.EV_ID,
          sujetoId = obn.BOB_SUJ_IDENTIFICADOR,
          objetoId = obn.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obn.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obn.BOB_OBN_ID,
          registro = obligacion,
          cuota = None
        )
      case obn: ObligacionesAnt =>
        ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = obligacion,
          //todo: fix
          detallesObligacion = detalles.getOrElse(Seq.empty),
          isAdheridoDebito = isAdheridoDebito
        )
    }
    actorRef.ask[Response.SuccessProcessing](command)
    //???
  }

  private def isNotDeuda(obligacion: ObligacionesAnt): Boolean = {

    //val otrosAtributos = extractOtrosAtributos(obligacion).getOrElse(default = Nil)

    val result: Boolean = {

      val ruleNumber = obligacion.RULE_NUMBER

      if (ruleNumber.contains("-1")) {
        true
      } else {
        false
      }
    }
    result
  }


}
