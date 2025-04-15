package consumers.no_registral.objeto.infrastructure.consumer

import akka.actor.{ActorRef, ActorSystem}
//import akka.pattern.ask
import akka.util.Timeout
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{ListDetallesObjeto, ObjetosTri}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import consumers.no_registral.objeto.infrastructure.sorter.ObjetoCommandRouter
import design_principles.actor_model.Response
import io.circe.parser.decode
import monitoring.Monitoring

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

// Consumer modificado para usar el router
case class ObjetoTributarioTransaction(actorRef: ActorRef, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements,
  system: ActorSystem  // Recibir el ActorSystem como parámetro implícito
) extends ActorTransaction[ObjetosTri](monitoring) {

  implicit val timeout: Timeout = Timeout(30.seconds)
  implicit val ec: ExecutionContext = actorTransactionRequirements.executionContext

  // Crear o recuperar el router
  val sorterEnabled: String = Try(System.getenv("BETTER_SORTER")).getOrElse("OFF")
  private val commandRouter = ObjetoCommandRouter.getOrCreate(system, actorRef)

  def topic = "DGR-COP-OBJETOS-TRI"
  def topicRetry = "DGR-COP-OBJETOS-TRI_retry"
  def topicError = "DGR-COP-OBJETOS-TRI_error"

  def processInput(input: String): Either[Throwable, ObjetosTri] =
    decode[ObjetosTri](input)

  def processMessage(registro: ObjetosTri): Future[Response.SuccessProcessing] = {
    val isResponsable: Option[ListDetallesObjeto] => List[Boolean] = {
      case Some(d) =>
        d.SOJ_DETALLES map { d =>
          d.RESPONSABLE_OTROS_ATRIBUTOS contains "S"
        }
      case None => List(false)
    }

    val sujetoResponsable: List[Option[String]] = registro.SOJ_OTROS_ATRIBUTOS match {
      case Some(r) =>
        r.SOJ_DETALLES map { d =>
          d.RESPONSABLE_OTROS_ATRIBUTOS.getOrElse("N") match {
            case "S" => Some(registro.SOJ_SUJ_IDENTIFICADOR)
            case "N" => None
            case _ => None
          }
        }
      case None => List(Some("N"))
    }

    val isAdheridoDebito = Some(registro.SOJ_ADHERIDO_DEBITO.contains("S"))

    if (registro.SOJ_SUJ_IDENTIFICADOR == "" || registro.SOJ_IDENTIFICADOR == "" || registro.SOJ_TIPO_OBJETO == "") {
      Future.failed(new IllegalArgumentException("Campos obligatorios vacíos, operación omitida"))
    } else {
      val command: ObjetoCommands =
        if (registro.SOJ_ESTADO.contains("BAJA"))
          ObjetoCommands.SetBajaObjeto(
            sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
            objetoId = registro.SOJ_IDENTIFICADOR,
            tipoObjeto = registro.SOJ_TIPO_OBJETO,
            deliveryId = registro.EV_ID,
            registro = registro,
            isResponsable = Some(isResponsable(registro.SOJ_OTROS_ATRIBUTOS).head),
            sujetoResponsable = sujetoResponsable.head
          )
        else
          ObjetoCommands.ObjetoUpdateFromTri(
            sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
            objetoId = registro.SOJ_IDENTIFICADOR,
            tipoObjeto = registro.SOJ_TIPO_OBJETO,
            deliveryId = registro.EV_ID,
            registro = registro,
            isResponsable = Some(isResponsable(registro.SOJ_OTROS_ATRIBUTOS).head),
            sujetoResponsable = sujetoResponsable.head,
            isAdheridoDebito = isAdheridoDebito
          )

      // Enviar el comando al router en lugar del actor directamente
      // El router se encargará de garantizar el procesamiento secuencial
      sorterEnabled.equals("ON") match {
        case true => {
          recordBetterSorter()
          commandRouter.ask[Response.SuccessProcessing](command)
        }
        case false => actorRef.ask[Response.SuccessProcessing](command)
      }
    }
  }
}