package consumers.registral.cupon_descuento.infrastructure.consumer

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoCommands, CuponDescuentoTri, DetallesCuponDescuento}
import consumers.registral.cupon_descuento.infrastructure.dependency_injection.CuponDescuentoActor
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import play.api.libs.json.Reads
import io.circe.parser._
import consumers.registral.cupon_descuento.infrastructure.json.json._
import io.circe.Encoder

import scala.concurrent.Future

case class CuponDescuentoTributarioTransaction(actor: CuponDescuentoActor, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[CuponDescuentoTri](monitoring) {
  private val log = LoggerFactory.getLogger(this.getClass)
  def topic = "DGR-COP-CUPON-DESCUENTO-TRI"
  def topicRetry = "DGR-COP-CUPON-DESCUENTO-TRI_retry"
  def topicError = "DGR-COP-CUPON-DESCUENTO-TRI_error"

  def processInput(input: String): Either[Throwable, CuponDescuentoTri] = {
    println("CUMBIA -> " + decode[CuponDescuentoTri](input))
    decode[CuponDescuentoTri](input)
  }

  override def processMessage(registro: CuponDescuentoTri): Future[Response.SuccessProcessing] = {

    val detalles = for {
      otrosAtributos <- registro.BOB_OTROS_ATRIBUTOS
      detalles = decode[Seq[DetallesCuponDescuento]](otrosAtributos.toString)

    } yield (detalles.getOrElse(Seq()))

    val command: CuponDescuentoCommands.CuponDescuentoUpdateFromDto =
      CuponDescuentoCommands.CuponDescuentoUpdateFromDto(
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
