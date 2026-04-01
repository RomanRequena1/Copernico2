package consumers.no_registral.objeto.infrastructure.sorter

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import design_principles.actor_model.Response
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Actor que procesa comandos para un único objetoId específico.
 * Esto garantiza procesamiento secuencial para el mismo objetoId.
 */
class SingleObjectProcessor(targetGlobalActor: ActorRef, objetoId: String) extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val ec = context.dispatcher

  private val processingTimeoutSeconds: Long = {
    val envValue = Option(System.getenv("SINGLE_OBJECT_PROCESSOR_TIMEOUT_SECONDS"))
    envValue match {
      case Some(s) =>
        scala.util.Try(s.toLong).toOption match {
          case Some(v) => v
          case None =>
            logger.warn(s"Invalid value for SINGLE_OBJECT_PROCESSOR_TIMEOUT_SECONDS: '$s'. Using default of 30 seconds.")
            30L
        }
      case None => 30L
    }
  }
  implicit val timeout: Timeout = processingTimeoutSeconds.seconds

  // Estado interno para seguimiento
  private var currentStateId = 0
  private var busy = false
  private var queue = List.empty[(ObjetoCommands, ActorRef)]

  def receive: Receive = {
    case cmd: ObjetoCommands if cmd.objetoId == objetoId =>
      if (busy) {
        // Si estamos ocupados, encolar para procesar después
        queue = queue :+ ((cmd, sender()))
      } else {
        processCommand(cmd, sender())
      }

    case "CommandProcessed" =>
      // Comando actual procesado, marcar como no ocupado
      busy = false

      // Procesar el siguiente comando en la cola si existe
      if (queue.nonEmpty) {
        val (nextCmd, originalSender) = queue.head
        queue = queue.tail
        processCommand(nextCmd, originalSender)
      }
  }

  private def processCommand(cmd: ObjetoCommands, originalSender: ActorRef): Unit = {
    busy = true
    currentStateId += 1

    // Enviar el comando al actor global y pipe la respuesta al remitente original
    (targetGlobalActor ? cmd)
      .mapTo[Response.SuccessProcessing]
      .recover {
        case e: Exception =>
          logger.error(s"Doing recover of better sorter Objeto: $objetoId, (queue: ${queue.size}) - ${e.getMessage}")
          Response.SuccessProcessing(s"ERROR-RECOVERED-${cmd.aggregateRoot}", cmd.deliveryId)
      }
      .map { response =>
        // Notificar a este actor que ha terminado de procesar
        self ! "CommandProcessed"
        response
      }
      .pipeTo(originalSender)
  }
}
