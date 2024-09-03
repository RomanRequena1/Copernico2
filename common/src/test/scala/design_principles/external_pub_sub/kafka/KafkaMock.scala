package design_principles.external_pub_sub.kafka

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.stream.{OverflowStrategy, UniqueKillSwitch}
import akka.stream.scaladsl.{Sink, Source, SourceQueue}
import api.actor_transaction.ActorTransaction
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{KafkaTransactionalMessageProcessor, MessageProcessor, MessageProducer}
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.mutable
import scala.util.{Failure, Success}

class KafkaMock() extends MessageProducer with MessageProcessor with MessageProcessorLogging {

  object PubSub {
    case class Message(topic: String, message: KafkaKeyValue)
    case class SubscribeMe(topic: String, algorithm: String => Future[Seq[String]])
  }
  import PubSub._
  var subscriptors: Set[SubscribeMe] = Set.empty

  def receive(message: Any): Any = {
    //println("rec " + message)
    message match {
      case m: Message if !(topics contains m.topic) =>
        println("1 - No Topic" + this.toString)
        //rintln("No topic - " + topics + " ---: " + m.message)
      case m: Message if topics contains m.topic =>
        //println("5 ---" + topics)
        println("5 --- " + this.toString)

        messageHistory = messageHistory :+ ((m.topic, m.message.json))
        //      println(
        //        s"""
        //           |${Console.YELLOW} [MessageProducer] ${Console.RESET}
        //           |Sending message to: ${subscriptors
        //             .filter(_.topic == m.topic)
        //             .map(_.topic)
        //             .map(Console.YELLOW + _ + Console.RESET)
        //             .mkString(",")}
        //           |${Console.CYAN} $message ${Console.RESET}
        //           |""".stripMargin
        //      )
        subscriptors.filter(_.topic == m.topic).foreach {
          _.algorithm(m.message.json)
        }
      case s: SubscribeMe =>
        println("6 ---")
        subscriptors = subscriptors + s
    }
  }
  var topics: Set[String] = Set.empty

  override def produce(data: Seq[KafkaKeyValue], topic: String)(handler: Seq[KafkaKeyValue] => Unit) = {
    data map {
      PubSub.Message(topic, _)
    } foreach { receive }
    handler(data)
    Future.successful(Done)
  }

  override type MessageProcessorKillSwitch = UniqueKillSwitch

  override def run(SOURCE_TOPIC: String, SINK_TOPIC: String, RETRY_TOPIC: String, ERROR_TOPIC: String, algorithm: String => Future[Seq[String]]) =
    (None, {
      receive(PubSub.SubscribeMe(SOURCE_TOPIC, algorithm))
      Future.successful(Done)
    })

  override def createTopic(topic: String): Future[Done] = {
    topics = topics + topic
    Future.successful(Done)
  }
}

object KafkaMock {

  implicit class MessageProcessorImplicits(messageConsumer: MessageProcessor) {
    def subscribeActorTransaction(SOURCE_TOPIC: String, actorTransaction: ActorTransaction[_])(
        implicit ec: ExecutionContext
    ): (_, Future[Done]) =
      messageConsumer match {

        case processor: KafkaTransactionalMessageProcessor =>
          processor.run(SOURCE_TOPIC,
                        SOURCE_TOPIC + "_done",
                        SOURCE_TOPIC + "_retry",
                        SOURCE_TOPIC + "_error",
                        message => actorTransaction.transaction(message).map(_ => Seq("Done")))

        case kafkaMock: KafkaMock =>
          //println("2")
          //println("st: " + SOURCE_TOPIC)
          (Done, {
            kafkaMock.receive(
              kafkaMock.PubSub.SubscribeMe(SOURCE_TOPIC,
                                           message => actorTransaction.transaction(message).map(_ => Seq("Done")))
            )
            Future(Done)
          })
        case _ =>
          println("44")
          (Done, Future(Done))
      }
  }
}
