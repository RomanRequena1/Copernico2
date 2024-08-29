package design_principles.external_pub_sub.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.UniqueKillSwitch
import akka.stream.scaladsl.Source
import api.actor_transaction.ActorTransaction
import kafka.KafkaMessageProcessorRequirements.bootstrapServers
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{
  KafkaCommittablePartitionedMessageProcessor,
  KafkaTransactionalMessageProcessor,
  MessageProcessor,
  MessageProducer
}
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class KafkaProduction(implicit system: ActorSystem)
    extends MessageProducer
    with MessageProcessor
    with MessageProcessorLogging {

  object PubSub {
    case class Message(topic: String, message: KafkaKeyValue)
    case class SubscribeMe(topic: String, algorithm: String => Future[Seq[String]])
  }
  import PubSub._
  var subscriptors: Set[SubscribeMe] = Set.empty

  def receive(message: Any): Any = {
    message match {
      case m: Message if !(topics contains m.topic) =>
//        println("1 - No Topic" + this.toString)
      case m: Message if topics contains m.topic =>
//        println("5 --- " + this.toString + m.toString)
        messageHistory = messageHistory :+ ((m.topic, m.message.json))
//        println(
//          s"""
//             |${Console.YELLOW} [MessageProducer] ${Console.RESET}
//             |Sending message to: ${subscriptors
//               .filter(_.topic == m.topic)
//               .map(_.topic)
//               .map(Console.YELLOW + _ + Console.RESET)
//               .mkString(",")}
//             |${Console.CYAN} $message ${Console.RESET}
//             |""".stripMargin
//        )
        subscriptors.filter(_.topic == m.topic).foreach {
          _.algorithm(m.message.json)
        }
      case s: SubscribeMe =>
//        println("6 ---")
        subscriptors = subscriptors + s
    }
  }

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)
    .withProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "100663296") //TODO changed the buffer memory config to reduce the latency
    .withProperty(ProducerConfig.ACKS_CONFIG, "0")
    .withProperty(ProducerConfig.LINGER_MS_CONFIG, "5")
    .withProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "120000")

  def produce(data: Seq[KafkaKeyValue], topic: String)(handler: Seq[KafkaKeyValue] => Unit): Future[Done] = {

    implicit val ec: ExecutionContextExecutor = system.getDispatcher

    val publication: Future[Done] = Source(data)
    // NOTE: If no partition is specified but a key is present a partition will be chosen
    // using a hash of the key. If neither key nor partition is present a partition
    // will be assigned in a round-robin fashion.
      .map { m =>
        new ProducerRecord[String, String](topic, m.aggregateRoot, m.json)
      }
      .runWith(Producer.plainSink(producerSettings))
    publication.onComplete {
      case Success(Done) =>
        data foreach { s =>
          receive(PubSub.Message(topic, s))

        }
        handler(data)
      case Failure(t) => println(t)
    }

    publication

  }

  override type MessageProcessorKillSwitch = UniqueKillSwitch

  override def run(SOURCE_TOPIC: String,
                   SINK_TOPIC: String,
                   RETRY_TOPIC: String,
                   ERROR_TOPIC: String,
                   algorithm: String => Future[Seq[String]]) =
    (None, {
      receive(PubSub.SubscribeMe(SOURCE_TOPIC, algorithm))
      Future.successful(Done)
    })

  var topics: Set[String] = Set.empty
  override def createTopic(topic: String): Future[Done] = {
    topics = topics + topic
    Future.successful(Done)
  }
}

object KafkaProduction {

  implicit class MessageProcessorImplicits(messageConsumer: MessageProcessor) {
    def subscribeActorTransaction(SOURCE_TOPIC: String, actorTransaction: ActorTransaction[_])(
        implicit ec: ExecutionContext
    ): (_, Future[Done]) =
      messageConsumer match {

        case processor: KafkaCommittablePartitionedMessageProcessor =>
//          println("1")

          processor.run(SOURCE_TOPIC,
                        SOURCE_TOPIC + "_done",
                        SOURCE_TOPIC + "_retry",
                        SOURCE_TOPIC + "_error",
                        message => actorTransaction.transaction(message).map(_ => Seq("Done")))

        case kafkaProduction: KafkaProduction =>
//          println("2 -- " + actorTransaction.toString + " -- " + SOURCE_TOPIC)
//          println("st: " + SOURCE_TOPIC)

          val a: (Done.type, Future[Done.type]) = (Done, {
            kafkaProduction.receive(
              kafkaProduction.PubSub.SubscribeMe(SOURCE_TOPIC,
                                                 message =>
                                                   actorTransaction
                                                     .transaction(message)
                                                     .map(_ => {
                                                       Seq("Done")
                                                     }))
            )
            Future(Done)
          })
          a

//          kafkaProduction.run(SOURCE_TOPIC,
//              SOURCE_TOPIC + "_done",
//              SOURCE_TOPIC + "_retry",
//              SOURCE_TOPIC + "_error",
//              message => actorTransaction.transaction(message).map(_ => Seq("Done")))

        case _ =>
          println("3")
          (Done, Future(Done))
      }
  }
}
