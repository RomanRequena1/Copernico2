package kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink}
import com.lightbend.cinnamon.akka.stream.CinnamonAttributes
import com.lightbend.cinnamon.akka.stream.CinnamonAttributes.{GraphWithInstrumented, SourceWithInstrumented}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class KafkaCommittablePartitionedMessageProcessor(
                                                   transactionRequirements: KafkaMessageProcessorRequirements
                                                 ) extends MessageProcessor {

  override type MessageProcessorKillSwitch = akka.stream.UniqueKillSwitch

  private val log = LoggerFactory.getLogger(this.getClass)

  // TODO Changed ec with my-dispatcher
  implicit val ec: ExecutionContext = transactionRequirements.executionContext
  // implicit val ec: ExecutionContext = transactionRequirements.system.dispatchers.lookup("my-dispatcher")


  private val config = ConfigFactory.load()
  private val appConfig = new KafkaConfig(config)

  def transactionalId: String = java.util.UUID.randomUUID().toString

  //def blocking[T](body: =>T): T = BlockContext.current.blockOn(body)(scala.concurrent.blocking())

  def run(
           SOURCE_TOPIC: String,
           SINK_TOPIC: String,
           algorithm: String => Future[Seq[String]]
         ): (Option[MessageProcessorKillSwitch], Future[Done]) = {

    val ProcessedMessagesCounter = transactionRequirements.monitoring.counter(
      s"$SOURCE_TOPIC-ProcessedMessagesCounter"
    )
    val RejectedMessagesCounter = transactionRequirements.monitoring.counter(
      s"$SOURCE_TOPIC-RejectedMessagesCounter"
    )
    type Msg = ConsumerMessage.TransactionalMessage[String, String]

    implicit val system: ActorSystem = transactionRequirements.system
    val consumer = transactionRequirements.consumer
    val producer = transactionRequirements.producer
    val rebalancerListener = transactionRequirements.rebalancerListener

    val subscription = Subscriptions.topics(SOURCE_TOPIC).withRebalanceListener(rebalancerListener)

    val NR_PARTITIONS: Int = appConfig.PARTITIONS_NUMBER

    val CONSUMER_PARALLELISM: Int = Try(System.getenv("CONSUMER_PARALLELISM")).map(_.toInt).getOrElse(1)

    val committerSettings = CommitterSettings(system)


    val config = system.settings.config.getConfig("akka.kafka.producer")


    val producerSettings =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        // TODO changed the buffer memory config to reduce the latency - Moved to kafka.conf
        // .withBootstrapServers(bootstrapServers)
        // .withProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "100663296")
        // .withProperty(ProducerConfig.ACKS_CONFIG, "0")
        // .withProperty(ProducerConfig.LINGER_MS_CONFIG, "5")
        // .withProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "120000")

    val consumerGroup = Consumer

      .committablePartitionedSource(consumer
        .withGroupId(appConfig.CONSUMER_GROUP), subscription)
        //.buffer(10000, OverflowStrategy.backpressure)
        // TODO changed to reduce the Consumer latency - Moved to kafka.conf
        //.withProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor")
        //.withProperty(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "120000")
        //.withProperty(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "5"), subscription)
        //.withProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor"), subscription)
      .mapAsyncUnordered(NR_PARTITIONS * CONSUMER_PARALLELISM) { case (topicPartition: TopicPartition, source) =>
        source
          //.buffer(CONSUMER_PARALLELISM * NR_PARTITIONS, OverflowStrategy.backpressure)
          //.async("akka.stream.blocking-io-dispatcher")
          //.buffer(10000, OverflowStrategy.backpressure)
          .addAttributes(CinnamonAttributes.instrumented(reportByName = true , perFlow = true, perConnection = true, perBoundary = true, traceable = true))
          .mapAsync(CONSUMER_PARALLELISM) { msg: ConsumerMessage.CommittableMessage[String, String] =>
            val message = msg

            val input: String = message.record.value

            log.debug(message.record.value)

            algorithm(input)
              .map { a: Seq[String] =>
                Right(message -> a)
              }
              .recover {
                case e: Exception =>
                  Left(message -> s"""
                     Error in flow:
                     ${e.getMessage}
                     For input:
                     $input
                 """)
              }
          }

          .map {
            case Left((message, cause)) =>
              log.error(cause)
              RejectedMessagesCounter.increment()
              val output = Seq(message.record.value)
              ProducerMessage.multi(
                records = output.map { o =>
                  new ProducerRecord(
                    SOURCE_TOPIC + "_retry",
                    message.record.key,
                    o
                  )
                }.toList, passThrough = message.committableOffset
              )
            case Right((message, output)) =>
              ProcessedMessagesCounter.increment()
              ProducerMessage.multi(
                records = output.map { o =>
                  new ProducerRecord(
                    SINK_TOPIC,
                    message.record.key,
                    o
                  )
                }.toList, passThrough = message.committableOffset
              )
          }

          .map(_.passThrough)
   //       .collect {
   //         case a: ProducerMessage.Envelope[_, String, _] =>
  //            a.parts.map(a => a.record.value)
  //        }

          .instrumentedRunWith(Committer.sink(committerSettings))(name = "CommittablePartitioned", perFlow = true, perConnection = true, perBoundary = true)
      }

      .viaMat(KillSwitches.single)(Keep.right)
      .withAttributes(akka.defaultSupervisionStrategy)
      .toMat(Sink.ignore)(Keep.both)
      .instrumented(name = "CommittablePartitioned-Ext", perFlow = true, perConnection = true, perBoundary = true)
    val (killSwitch, done) = consumerGroup.run()


    done.onComplete {
      case Success(_) =>
        log.warn(s"""
                    |     Stream completed with success
                    |     This is caused by the HTTP endpoint /kafka/stop/$SOURCE_TOPIC
                    |     Because of this we will take no action to interfere:
                    |     The topic will not be restarted on it's own.
          """.stripMargin)
        killSwitch.shutdown()
      case Failure(ex) =>
        log.error(s"Stream completed with failure -- ${ex.getMessage}")
        killSwitch.shutdown()
        run(SOURCE_TOPIC, SINK_TOPIC, algorithm)
    }
    (Some(killSwitch), done)

  }
}
