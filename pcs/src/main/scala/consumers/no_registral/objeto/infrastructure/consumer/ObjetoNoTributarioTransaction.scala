package consumers.no_registral.objeto.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosAnt
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.Future

case class ObjetoNoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetosAnt](monitoring) {

  def topic = "DGR-COP-OBJETOS-ANT"

  def topicRetry = "DGR-COP-OBJETOS-ANT_retry"

  def topicError = "DGR-COP-OBJETOS-ANT_error"

  def processInput(input: String): Either[Throwable, ObjetosAnt] = {
//    println("CUMBIA Process input" + input)
    decode[ObjetosAnt](input)
  }

  def processMessage(registro: ObjetosAnt): Future[Response.SuccessProcessing] = {
    //connOracleKafkaToWriteside(registro.EV_ID.toString(), "objeto", registro.SOJ_CANAL_ORIGEN.getOrElse("TAX"))
//    println("CUMBIA Process message" + registro)

    val isResponsable = registro.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES map { n =>
      n.RESPONSABLE_OTROS_ATRIBUTOS contains "S"
    }
    val sujetoResponsable = registro.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES map { d =>
      d.RESPONSABLE_OTROS_ATRIBUTOS.getOrElse("N") match {
        case "S" => Some(registro.SOJ_SUJ_IDENTIFICADOR)
        case "N" => None
      }
    }

    val isAdheridoDebito = Some(registro.SOJ_ADHERIDO_DEBITO.contains("S"))

    val command: ObjetoCommands =
      if (registro.SOJ_ESTADO.contains("BAJA"))
        ObjetoCommands.SetBajaObjeto(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro,
          isResponsable = Some(isResponsable.head),
          sujetoResponsable = sujetoResponsable.head
        )
      else
        ObjetoCommands.ObjetoUpdateFromAnt(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro,
          isResponsable = Some(isResponsable.head),
          sujetoResponsable = sujetoResponsable.head,
          isAdheridoDebito = isAdheridoDebito
        )
    actorRef.ask[Response.SuccessProcessing](command)
  }
}
