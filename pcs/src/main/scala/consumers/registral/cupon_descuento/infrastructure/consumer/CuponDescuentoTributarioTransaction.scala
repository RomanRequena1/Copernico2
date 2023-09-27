package consumers.registral.cupon_descuento.infrastructure.consumer

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoCommands
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.{CuponDescuentoTri, DetallesCuponDescuento}
import consumers.registral.cupon_descuento.infrastructure.dependency_injection.CuponDescuentoActor
import design_principles.actor_model.Response
import monitoring.Monitoring
import play.api.libs.json.Reads
import serialization.maybeDecode
import consumers.registral.cupon_descuento.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import org.slf4j.LoggerFactory

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
    maybeDecode[CuponDescuentoTri](input)
  }

  override def processMessage(registro: CuponDescuentoTri): Future[Response.SuccessProcessing] = {
    implicit val b: Reads[Seq[DetallesCuponDescuento]] = Reads.seq(DetallesCuponDescuentoF.reads)
    val detalles: Option[Seq[DetallesCuponDescuento]] = for {
      bobDetalles <- (registro.BOB_OTROS_ATRIBUTOS.get \ "BOB_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesCuponDescuento]](bobDetalles.toString())
    } yield detalles

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
