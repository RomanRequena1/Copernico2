package consumers.no_registral.obligacion.infrastructure.consumer
 import akka.actor.ActorRef
 import api.actor_transaction.ActorTransaction
 import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
 import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{ObligacionRemove, ObligacionUpdateFromDto}
 import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.ObligacionesAnt
 import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
 import design_principles.actor_model.Response
 import io.circe.parser.decode
 import monitoring.Monitoring

 import scala.concurrent.Future

case class ObligacionNoTributariaTransaction(actorRef: ActorRef, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesAnt](monitoring) {
  def topic = "DGR-COP-OBLIGACIONES-ANT"

  def topicRetry = "DGR-COP-OBLIGACIONES-ANT_retry"

  def topicError = "DGR-COP-OBLIGACIONES-ANT_error"

  def processInput(input: String): Either[Throwable, ObligacionesAnt] = {
    println("CUMBIA -> " + decode[ObligacionesAnt](input))
    decode[ObligacionesAnt](input)
  }

  def processMessage(obligacion: ObligacionesAnt): Future[Response.SuccessProcessing] = {
    val isNotDeuda: List[Boolean] = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES map {
      d => d.RULE_NUMBER.contains("-1")
    }

    val isCancelada: Seq[Boolean] = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.map {
      d => d.RULE_NUMBER.contains("-2")
    }

    val isAdheridoDebito = Some(obligacion.BOB_ADHERIDO_DEBITO.contains("S"))
    val command = obligacion match {
      case obn: ObligacionesAnt if isNotDeuda.head =>
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
          detallesObligacion = obligacion.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES,
          isAdheridoDebito = isAdheridoDebito
        )
    }
    actorRef.ask[Response.SuccessProcessing](command)
    //???
  }


}

