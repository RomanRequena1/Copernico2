package consumers.no_registral.obligacion.infrastructure.consumer
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.dmn.DMNTreintaPorciento
import consumers.no_registral.obligacion.application.entities
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{
  ObligacionAntUpdateFromDto,
  ObligacionRemove,
  ObligacionUpdateFromDto
}
import consumers.no_registral.obligacion.application.entities.{
  DetallesObligacion,
  DetallesSupresiones,
  ListDetallesObligaciones,
  ObligacionCommands,
  ObligacionesAnt
}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import monitoring.Monitoring
import org.camunda.dmn.DmnEngine
import org.camunda.dmn.parser.ParsedDmn
import scalaz.\/

import java.io.FileInputStream
import scala.concurrent.Future

case class ObligacionNoTributariaTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesAnt](monitoring) {

  def topic = "DGR-COP-OBLIGACIONES-ANT"

  def topicRetry = "DGR-COP-OBLIGACIONES-ANT_retry"

  def topicError = "DGR-COP-OBLIGACIONES-ANT_error"

  def processInput(input: String): Either[Throwable, ObligacionesAnt] = {
    decode[ObligacionesAnt](input)
  }

  def processMessage(obligacion: ObligacionesAnt): Future[Response.SuccessProcessing] = {
    val isNotDeuda: Option[ListDetallesObligaciones] => List[Boolean] = {
      case Some(d) =>
        d.BOB_DETALLES map { d =>
          d.RULE_NUMBER.contains("-1")
        }
      case None => List(false)
    }
    val isCancelada: Option[ListDetallesObligaciones] => List[Boolean] = {
      case Some(d) =>
        d.BOB_DETALLES map { d =>
          d.RULE_NUMBER.contains("-2")
        }
      case None => List(false)
    }

    val detallesObligacion: Seq[DetallesObligacion] = obligacion.BOB_OTROS_ATRIBUTOS match {
      case Some(r) => r.BOB_DETALLES
      case None => null
    }

    val detallesSupresiones: Seq[DetallesSupresiones] = obligacion.BOB_SUPRESIONES match {
      case Some(r) => r.BOB_DETALLES_SUPRESIONES
      case None => null
    }

    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))

    if (obligacion.BOB_SUJ_IDENTIFICADOR == "" || obligacion.BOB_SOJ_IDENTIFICADOR == "" || obligacion.BOB_SOJ_TIPO_OBJETO == "" || obligacion.BOB_OBN_ID == "") {
      Future.failed(new IllegalArgumentException("Campos obligatorios vacíos, operación omitida"))
    } else {
      val command: ObligacionCommands =
        if (isCancelada(obligacion.BOB_OTROS_ATRIBUTOS).head) {
          ObligacionCommands.ObligacionRemove(
            deliveryId = obligacion.EV_ID,
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            registro = obligacion,
            cuota = obligacion.BOB_CUOTA
          )
        } else if (isNotDeuda(obligacion.BOB_OTROS_ATRIBUTOS).head) {
          ObligacionCommands.ObligacionRemove(
            deliveryId = obligacion.EV_ID,
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            registro = obligacion,
            cuota = obligacion.BOB_CUOTA
          )
        } else {
          ObligacionAntUpdateFromDto(
            sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
            objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
            tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
            obligacionId = obligacion.BOB_OBN_ID,
            deliveryId = obligacion.EV_ID,
            registro = obligacion,
            detallesObligacion = detallesObligacion,
            detallesSupresiones = detallesSupresiones,
            isAdheridoDebito = isAdheridoDebito,
            cuota = obligacion.BOB_CUOTA
          )
        }
      actorRef.ask[Response.SuccessProcessing](command)
    }
  }
}
