package kafka

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}
import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import kafka.KafkaMessageProcessorRequirements.{PSRMbootstrapServers, PSRMEnabled, bootstrapServers}
import kafka.KafkaMessageProducer.KafkaKeyValue
import monitoring.Monitoring
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}

class KafkaMessageProducer()(
  implicit
  system: ActorSystem,
  producerSettings: ProducerSettings[String, String]
) extends MessageProducer {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def createTopic(topic: String): Future[Done] = Future.successful(Done)

  def produce(data: Seq[KafkaKeyValue], topic: String)(handler: Seq[KafkaKeyValue] => Unit): Future[Done] = {

    implicit val ec: ExecutionContextExecutor = system.getDispatcher

    println(s"[KAFKA_PRODUCE] 📤 Publicando a topic: $topic")
    println(s"[KAFKA_PRODUCE] 🔍 Mensajes: ${data.size}")

    val publication: Future[Done] = Source(data)
      .map { m =>
        new ProducerRecord[String, String](topic, m.aggregateRoot, m.json)
      }
      .runWith(Producer.plainSink(producerSettings))

    publication.onComplete {
      case Success(Done) =>
        println(s"[KAFKA_PRODUCE] ✅ Publicación EXITOSA a topic: $topic")
        data foreach { s =>
          log.debug(s"""Published $s to $topic""")
        }
        handler(data)

      case Failure(t) =>
        println(s"[KAFKA_PRODUCE] ❌ ERROR en publicación a topic: $topic")
        println(s"[KAFKA_PRODUCE] ❌ Error: ${t.getMessage}")
        log.error(s"An error has occurred publishing to $topic: " + t.getMessage, t)
    }

    publication
  }
}

object KafkaMessageProducer {

  case class KafkaKeyValue(aggregateRoot: String, json: String) {
    def key = aggregateRoot
    def value = json
  }

  def apply(monitoring: Monitoring,
            rebalancerListener: ActorRef)(implicit system: ActorSystem): KafkaMessageProducer = {
    println(s"[KAFKA_PRODUCER] 🚀 Creando producer PRINCIPAL con broker: $bootstrapServers")

    implicit def producerSettings: ProducerSettings[String, String] =
      ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers(bootstrapServers)
        .withProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "100663296")
        .withProperty(ProducerConfig.ACKS_CONFIG, "0")
        .withProperty(ProducerConfig.LINGER_MS_CONFIG, "5")
        .withProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "120000")

    println(s"[KAFKA_PRODUCER] ✅ Producer PRINCIPAL creado")
    new KafkaMessageProducer()
  }

  def psrmProducer(monitoring: Monitoring,
                   rebalancerListener: ActorRef)(implicit system: ActorSystem): Try[KafkaMessageProducer] = Try {

    println(s"========================================")
    println(s"[PSRM_PRODUCER] 🔍 Validando configuración PSRM...")
    println(s"[PSRM_PRODUCER]    - Habilitado: $PSRMEnabled")
    println(s"[PSRM_PRODUCER]    - Broker: ${PSRMbootstrapServers.getOrElse("NO CONFIGURADO")}")

    if (!PSRMEnabled) {
      println(s"[PSRM_PRODUCER] ⚠️ PSRM deshabilitado por configuración")
      println(s"========================================")
      throw new IllegalStateException("PSRM producer está deshabilitado")
    }

    val broker = PSRMbootstrapServers.getOrElse {
      println(s"[PSRM_PRODUCER] ❌ ERROR: Broker PSRM no configurado")
      println(s"========================================")
      throw new IllegalArgumentException("KAFKA_BROKERS_LIST_PSRM no está configurado")
    }

    println(s"[PSRM_PRODUCER] 🚀 Creando producer PSRM con broker: $broker")

    implicit def producerSettings: ProducerSettings[String, String] =
      ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers(broker)
        .withProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, "100663296")
        .withProperty(ProducerConfig.ACKS_CONFIG, "0")
        .withProperty(ProducerConfig.LINGER_MS_CONFIG, "5")
        .withProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, "120000")

    println(s"[PSRM_PRODUCER] ✅ Producer PSRM creado exitosamente")
    println(s"========================================")

    new KafkaMessageProducer()
  }
}