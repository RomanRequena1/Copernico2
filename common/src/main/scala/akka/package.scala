import akka.actor.ActorRef
import akka.pattern.{ask => AkkaAsk}
import akka.stream.{ActorAttributes, Supervision}
import akka.util.Timeout
import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.Try
package object akka {

  type PersistenceId = String

  implicit class AlternativeAskSintax(actorRef: ActorRef) {
   val time: Int = Try(System.getenv("TIMEOUT").toInt).getOrElse(300)
    implicit val timeout: Timeout = time seconds
    def ask[Output: ClassTag](message: Any): Future[Output] = (actorRef ? message).mapTo[Output]
  }

  val defaultSupervisionStrategy = ActorAttributes.supervisionStrategy(
    {
      case error: Exception =>
        log.error(s"""Akka Decider survived exception: ${error.getMessage}
             ${error.getStackTrace.mkString("\n")}
             """)
        Supervision.Resume
      case error: Throwable =>
        log.error(s"""Akka Decider survived throwable: ${error.getMessage}
             ${error.getStackTrace.mkString("\n")}
             """)
        Supervision.Resume
    }
  )
  @JsonIgnore
  val log = LoggerFactory.getLogger(this.getClass)

}
