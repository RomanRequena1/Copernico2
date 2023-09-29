package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.Future
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetosTri, ObjetosTriOtrosAtributos}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import design_principles.actor_model.Response
import monitoring.Monitoring
import io.circe.parser.decode

case class ObjetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetosTri](monitoring) {

  def topic = "DGR-COP-OBJETOS-TRI"
  def topicRetry = "DGR-COP-OBJETOS-TRI_retry"
  def topicError = "DGR-COP-OBJETOS-TRI_error"

  def processInput(input: String): Either[Throwable, ObjetosTri] =
    decode[ObjetosTri](input)


  def processMessage(registro: ObjetosTri): Future[Response.SuccessProcessing] = {
    //connOracleKafkaToWriteside(registro.EV_ID.toString(), "objeto", registro.SOJ_CANAL_ORIGEN.getOrElse("TAX"))


    val detalle: Option[ObjetosTriOtrosAtributos] = for {
      otrosAtributos <- registro.SOJ_OTROS_ATRIBUTOS

      detalles = decode[Seq[ObjetosTriOtrosAtributos]](otrosAtributos.toString)
    } yield detalles.getOrElse(Seq()).head

    val isResponsable = detalle map { d =>
      d.RESPONSABLE_OTROS_ATRIBUTOS contains "S"
    }
    val sujetoResponsable = detalle flatMap { d =>
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
          isResponsable = isResponsable,
          sujetoResponsable = sujetoResponsable
        )
      else
        ObjetoCommands.ObjetoUpdateFromTri(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro,
          isResponsable = isResponsable,
          sujetoResponsable = sujetoResponsable,
          isAdheridoDebito = isAdheridoDebito
        )

    actorRef.ask[Response.SuccessProcessing](command)
  }
}
