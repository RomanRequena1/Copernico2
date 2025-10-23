package consumers.no_registral.objeto.infrastructure.sorter

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import design_principles.actor_model.Response
import monitoring.Monitoring
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
/**
 * Actor que procesa comandos para un único objetoId específico.
 * Esto garantiza procesamiento secuencial para el mismo objetoId.
 */
class SingleObjectProcessor(targetGlobalActor: ActorRef, objetoId: String) extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val timeout: Timeout = 30 seconds
  implicit val ec = context.dispatcher

  // Estado interno para seguimiento
  private var currentStateId = 0
  private var busy = false
  private var queue = List.empty[(ObjetoCommands, ActorRef)]

  def receive: Receive = {
    case cmd: ObjetoCommands if cmd.objetoId == objetoId =>
      if (busy) {
        // Si estamos ocupados, encolar para procesar después
//        logger.debug(s"Actor para objetoId $objetoId está ocupado, encolando comando")
        queue = queue :+ ((cmd, sender()))
      } else {
        processCommand(cmd, sender())
      }

    case "CommandProcessed" =>
      // Comando actual procesado, marcar como no ocupado
      busy = false

      // Procesar el siguiente comando en la cola si existe
      if (queue.nonEmpty) {
//        logger.debug(s"Procesando siguiente comando en cola para objetoId $objetoId")
        val (nextCmd, originalSender) = queue.head
        queue = queue.tail
        processCommand(nextCmd, originalSender)
      }
  }

  private def processCommand(cmd: ObjetoCommands, originalSender: ActorRef): Unit = {
    busy = true
    currentStateId += 1

    // Log antes de enviar
//    logger.debug(
//      f"""|CUMBIA
//          |  | command_id: ${cmd.deliveryId}%-20s | state_id: ${currentStateId}%-5s
//          |  | sender    : ${sender().path}
//          |  | self      : ${self.path}
//          |""".stripMargin
//    )

    // Enviar el comando al actor global y pipe la respuesta al remitente original
    (targetGlobalActor ? cmd)(400 seconds)
      .mapTo[Response.SuccessProcessing]
      .recover {
        case e: Exception => {
          logger.error(s"Doing recover of better sorter Objeto: $objetoId, (queue: ${queue.size}) - ${e.getMessage}")
          self ! "CommandProcessed"
        }
      }
      .map { response =>
        // Notificar a este actor que ha terminado de procesar
        self ! "CommandProcessed"

        // Retornar la respuesta original
        response
      }
      .pipeTo(originalSender)
  }
}