package consumers.no_registral.obligacion.infrastructure.sorter

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import design_principles.actor_model.Response
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps
/**
 * Actor que procesa comandos para un único obligacionId específico.
 * Esto garantiza procesamiento secuencial para el mismo objetoId.
 */
class SingleObligacionProcessor(targetGlobalActor: ActorRef, obligacionId: String) extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val timeout: Timeout = 30 seconds
  implicit val ec = context.dispatcher

  private var currentStateId = 0
  private var busy = false
  private var queue = List.empty[(ObligacionCommands, ActorRef)]

  def receive: Receive = {
    case cmd: ObligacionCommands if cmd.obligacionId == obligacionId =>
      if (busy) {
        logger.debug(s"Actor para obligacionId $obligacionId está ocupado, encolando comando")
        queue = queue :+ ((cmd, sender()))
      } else {
        processCommand(cmd, sender())
      }

    case "CommandProcessed" =>
      busy = false

      if (queue.nonEmpty) {
        logger.debug(s"Procesando siguiente comando en cola para obligacionId $obligacionId")
        val (nextCmd, originalSender) = queue.head
        queue = queue.tail
        processCommand(nextCmd, originalSender)
      }
  }

  private def processCommand(cmd: ObligacionCommands, originalSender: ActorRef): Unit = {
    busy = true
    currentStateId += 1

    logger.debug(
      f"""|CUMBIA
          |  | command_id: ${cmd.deliveryId}%-20s | state_id: ${currentStateId}%-5s
          |  | sender    : ${sender().path}
          |  | self      : ${self.path}
          |""".stripMargin
    )

    (targetGlobalActor ? cmd)(400 seconds)
      .mapTo[Response.SuccessProcessing].recover {
        case e: Exception => {
          logger.error(s"Doing recover of better sorter Obligacion: $obligacionId, (queue: ${queue.size}) - ${e.getMessage}")
          self ! "CommandProcessed"
        }
      }
      .map { response =>
        self ! "CommandProcessed"
        response
      }
      .pipeTo(originalSender)
  }
}