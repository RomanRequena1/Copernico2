package consumers.no_registral.sujeto.infrastructure.http

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionReprocess
import design_principles.actor_model.Response

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object ObligacionTaggingActor {
  def props(actor: ActorRef)(implicit system: ActorSystem): Props =
    Props(new ObligacionTaggingActor(actor))
}

class ObligacionTaggingActor(actor: ActorRef)(implicit system: ActorSystem)
  extends Actor with ActorLogging {

  implicit val ec: ExecutionContext = context.dispatcher

  private val readJournal = PersistenceQuery(system)
    .readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  override def receive: Receive = {
    case StartReprocessing() =>
      println("Starting obligacion events reprocessing")
      val originalSender = sender()

      reprocessAllObligacionEvents().onComplete {
        case Success(done) =>
          originalSender ! ReprocessingCompleted(done)
        case Failure(error) =>
          originalSender ! ReprocessingFailed(error)
      }

    case StopReprocessing() =>
      println("Stopping obligacion tagging actor")
      context.stop(self)

    case PoisonPill =>
      println("Obligacion tagging actor received PoisonPill")
      context.stop(self)
  }

  private def reprocessAllObligacionEvents() = {
    readJournal
      .currentPersistenceIds()
      .filter {
        case s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId" => true
        case _ => false
      }
      .mapAsyncUnordered(100) { persistenceId =>
        sendReprocessCommandToActor(persistenceId)
      }
      .runWith(Sink.ignore)
  }

  private def sendReprocessCommandToActor(persistenceId: String) = {
    persistenceId match {
      case s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId" =>
        val command = ObligacionReprocess(
          deliveryId = 0,
          sujetoId = sujetoId,
          objetoId = objetoId,
          tipoObjeto = tipoObjeto,
          obligacionId = obligacionId
        )
        actor.ask[Response.SuccessProcessing](command)
    }
  }
}
