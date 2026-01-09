package api.actor_transaction

import akka.actor.Cancellable
import akka.http.Controller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, path, _}
import akka.http.scaladsl.server.Route
import akka.kafka.scaladsl.MetadataClient
import akka.stream.KillSwitch
import kafka.{KafkaCommittablePartitionedMessageProcessor, KafkaMessageProcessorRequirements}
import org.apache.kafka.common.TopicPartition

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.util.Try

class ActorTransactionController(
    actorTransaction: ActorTransaction[_],
    requirements: KafkaMessageProcessorRequirements
) extends Controller(requirements.monitoring) {

  implicit val system = requirements.system
  //implicit private val ec: ExecutionContextExecutor = system.dispatcher
  implicit private val ec: ExecutionContextExecutor = system.dispatchers.lookup("my-dispatcher")

  var currentTransaction: Option[KillSwitch] = None
  var shouldBeRunning: Boolean = false

  var schedulerCancellable: Option[Cancellable] = None

  private def startMessageBehindScheduler(topic: String, interval: Int): Option[Cancellable] = {
    val metadataClient = MetadataClient.create(requirements.consumer, 20.second)
    val messageBehindFull: String = Option(System.getenv("MESSAGE_BEHIND_1_FULL")).getOrElse("ON")

    val cancellable: Cancellable = system.scheduler.scheduleWithFixedDelay(
      initialDelay = 300.seconds,
      delay = interval.seconds
    ) { () =>
      try {
        metadataClient.getPartitionsFor(topic).flatMap { partitions =>
          val topicPartitions = partitions.map(p =>
            new TopicPartition(topic, p.partition())
          ).toSet
          metadataClient.getCommittedOffsets(topicPartitions).flatMap { committedOffsets =>
            metadataClient.getEndOffsets(topicPartitions).map { endOffsets =>
              val totalLag = topicPartitions.map { tp =>
                val committed = committedOffsets.get(tp).map(_.offset()).getOrElse(0L)
                val end = endOffsets.getOrElse(tp, 0L)
                if (messageBehindFull.equals("ON")) {
                  requirements.monitoring.gauge(s"$topic-message-behind-end", Map.apply(("partition", tp.partition().toString))).set(end)
                  requirements.monitoring.gauge(s"$topic-message-behind-committed", Map.apply(("partition", tp.partition().toString))).set(committed)
                }
                Math.max(0, end - committed)
              }.sum

              requirements.monitoring.gauge(s"$topic-message-behind").set(totalLag)
              totalLag
            }
          }
        }.recover {
          case ex: Exception =>
            log.error(s"Error calculating message lag for $topic - (Restaring): ${ex.getMessage}")
            schedulerCancellable.foreach(_.cancel())
            schedulerCancellable = startMessageBehindScheduler(topic, interval)
            0L
        }
      } catch {
        case ex: Exception =>
          log.error(s"Scheduler metrics behind execution failed for $topic: ${ex.getMessage}")
          if (shouldBeRunning) {
            schedulerCancellable.foreach(_.cancel())
            schedulerCancellable = startMessageBehindScheduler(topic, interval)
          }
      }
    }
    Some(cancellable)
  }

  def stopTransaction(): Unit = {
    currentTransaction = currentTransaction match {
      case Some(killswitch) =>
        log.debug(s"${actorTransaction.topic} transaction stopped.")
        killswitch.shutdown()
        log.debug("Setting currentTransaction to None")

        None
      case None =>
        log.debug(s"${actorTransaction.topic} transaction was already stopped!")
        None
    }
  }

  def startTransaction(): Option[KillSwitch] = {
    def topic = actorTransaction.topic
    def topicRetry= actorTransaction.topicRetry
    def topicError= actorTransaction.topicError

    val transaction = actorTransaction.transaction _
    log.debug(s"Starting ${actorTransaction.topic} transaction")

    //TODO changed to KafkaCommittablePartitionedMessageProcessor

    val (killSwitch, done) = new KafkaCommittablePartitionedMessageProcessor(requirements)
    //  val (killSwitch, done) = new KafkaTransactionalMessageProcessor(requirements)
    // val (killSwitch, done) = new KafkaCommitableMessageProcessor(requirements)
    //  val (killSwitch, done) = new KafkaCommittableSourceMessageProcessor(requirements)
    // val (killSwitch, done) = new KafkaPlainConsumerMessageProcessor(requirements)
      .run(topic, s"${topic}SINK",topicRetry,topicError, message => {
        transaction(message).map { output =>
          Seq(output.toString)
        }
      })

    val messageBehindEnabled: String = Option(System.getenv("MESSAGE_BEHIND_1_ENABLED")).getOrElse("ON")
    val messageBehindInterval: Int = Option(System.getenv("MESSAGE_BEHIND_INTERVAL_SECONDS")).flatMap(s => Try(s.toInt).toOption).getOrElse(60)

    if (messageBehindEnabled.equals("ON")) {
      log.info(s"Message behind metrics enabled for $topic with interval $messageBehindInterval seconds")
      startMessageBehindScheduler(topic, messageBehindInterval)
    }

    done.onComplete { result =>
      // restart if the flag is set to true
      if (shouldBeRunning) {
        log.debug(s"Transaction finished with $result. Restarting it.")
        stopTransaction()
        startTransaction()
      }
    }
    log.debug("Setting currentTransaction to Some(killswitch)")
    currentTransaction = killSwitch
    killSwitch
  }
  def route: Route =
    path("api" / "system" / "health" / "topic" / actorTransaction.topic) {

      complete(StatusCodes.OK)
    }
}
