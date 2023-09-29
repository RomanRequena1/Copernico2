package consumers.registral.componente_i.infrastructure.consumer

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.componente_i.application.entities.{ComponenteICommands, ComponenteITri, DetallesComponenteI}
import consumers.registral.componente_i.infrastructure.dependency_injection.ComponenteIActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import play.api.libs.json.Reads
import io.circe.parser._
import consumers.registral.componente_i.infrastructure.json._
import io.circe.Encoder

import scala.concurrent.Future

case class ComponenteITributarioTransaction(actor: ComponenteIActor, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ComponenteITri](monitoring) {
  private val log = LoggerFactory.getLogger(this.getClass)
  def topic = "DGR-COP-COMPONENTE-I-TRI"
  def topicRetry = "DGR-COP-COMPONENTE-I-TRI_retry"
  def topicError = "DGR-COP-COMPONENTE-I-TRI_error"

  def processInput(input: String): Either[Throwable, ComponenteITri] = {
    decode[ComponenteITri](input)
  }

  override def processMessage(registro: ComponenteITri): Future[Response.SuccessProcessing] = {
    implicit val b: Encoder[Seq[DetallesComponenteI]] = Encoder(DetallesComponenteIEncoder)
    val detalles: Option[Seq[DetallesComponenteI]] = for {
      bobDetalles <- (registro.BOB_OTROS_ATRIBUTOS.get \ "BOB_DETALLES").toOption
      detalles = decode[Seq[DetallesComponenteI]](bobDetalles.toString())
    } yield detalles

    val command: ComponenteICommands.ComponenteIUpdateFromDto =
      ComponenteICommands.ComponenteIUpdateFromDto(
        deliveryId = BigInt(registro.EV_ID.bigInteger),
        sujetoId = registro.BOB_SUJ_IDENTIFICADOR,
        objetoId = registro.BOB_SOJ_IDENTIFICADOR,
        tipoObjeto = registro.BOB_SOJ_TIPO_OBJETO,
        obligacionId = registro.BOB_OBN_ID,
        registro = registro,
        detalles.getOrElse(Seq.empty)
      )

    actor.ask(command)
  }

}
