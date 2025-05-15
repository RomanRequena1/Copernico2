package consumers.no_registral.obligacion.infrastructure.sorter

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import org.slf4j.LoggerFactory
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Este actor actúa como un router que garantiza que los comandos para
 * el mismo obligacionId se procesen secuencialmente.
 */
class ObligacionCommandRouter(targetGlobalActor: ActorRef) extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val timeout: Timeout = 30 seconds
  implicit val ec = context.dispatcher

  // Mapa para almacenar actores por obligacionId
  private var obligationActors = Map.empty[String, ActorRef]

  def receive: Receive = {
    case cmd: ObligacionCommands =>
      val obligacionId = cmd.obligacionId

      logger.debug(s"Router recibió comando para obligacionId: $obligacionId")

      // Obtener o crear un actor para este obligacionId
      val actor = obligationActors.getOrElse(obligacionId, {
        logger.debug(s"Creando nuevo actor para obligacionId: $obligacionId")
        val newActor = context.actorOf(
          Props(new SingleObligationProcessor(targetGlobalActor, obligacionId)),
          s"obligation-processor-$obligacionId"
        )
        obligationActors += (obligacionId -> newActor)
        newActor
      })

      // Reenviar el comando al actor para esta obligacion
      actor.forward(cmd)
  }
}


/**
 * Obligacion companion para facilitar la creación del router
 */
object ObligacionCommandRouter {
  def props(targetGlobalActor: ActorRef): Props = Props(new ObligacionCommandRouter(targetGlobalActor))

  // Método para crear o recuperar la instancia del router
  def getOrCreate(system: akka.actor.ActorSystem, targetGlobalActor: ActorRef): ActorRef = {
    val routerName = "obligacion-command-router"

    try {
      // Intentar recuperar el router existente
      implicit val timeout = Timeout(5.seconds)
      val selection = system.actorSelection(s"/user/$routerName")
      val future = selection.resolveOne()
      Await.result(future, 5.seconds)
    } catch {
      case _: Exception =>
        system.actorOf(props(targetGlobalActor), routerName)
    }
  }
}