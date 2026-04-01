package consumers.no_registral.objeto.infrastructure.sorter

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Este actor actúa como un router que garantiza que los comandos para
 * el mismo objetoId se procesen secuencialmente.
 */
class ObjetoCommandRouter(targetGlobalActor: ActorRef) extends Actor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  implicit val timeout: Timeout = 30 seconds
  implicit val ec = context.dispatcher

  // Mapa para almacenar actores por objetoId
  private var objectActors = Map.empty[String, ActorRef]

  def receive: Receive = {
    case cmd: ObjetoCommands =>
      val objetoId = cmd.objetoId

      // Obtener o crear un actor para este objetoId
      val actor = objectActors.getOrElse(objetoId, {
        val newActor = context.actorOf(
          Props(new SingleObjectProcessor(targetGlobalActor, objetoId)),
          s"object-processor-$objetoId"
        )
        objectActors += (objetoId -> newActor)
        newActor
      })

      // Reenviar el comando al actor para este objeto
      actor.forward(cmd)
  }
}


/**
 * Objeto companion para facilitar la creación del router
 */
object ObjetoCommandRouter {
  def props(targetGlobalActor: ActorRef): Props = Props(new ObjetoCommandRouter(targetGlobalActor))

  // Método para crear o recuperar la instancia del router
  def getOrCreate(system: akka.actor.ActorSystem, targetGlobalActor: ActorRef): ActorRef = {
    val routerName = "objeto-command-router"

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
