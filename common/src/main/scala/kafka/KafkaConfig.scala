package kafka

import scala.util.Try
import com.typesafe.config.Config

class KafkaConfig(config: Config) {

  lazy val KAFKA_BROKER: String = {
    val broker = Try { config.getString("kafka.brokers") }.getOrElse("0.0.0.0:9092")
    broker
  }

  lazy val PSRM_ENABLED: Boolean = {
    val enabled = Try { config.getBoolean("kafka.psrm.enabled") }.getOrElse(false)
    enabled
  }

  lazy val KAFKA_PSRM_BROKER: Option[String] = {
    val envVar = sys.env.get("KAFKA_BROKERS_LIST_PSRM")
    val fromConfig = Try { config.getString("kafka.psrm.brokers") }.toOption

    val broker = fromConfig.orElse(envVar)

    if (broker.exists(b => b == "0.0.0.0:9092" || b.isEmpty)) {
      None
    } else {
      broker
    }
  }

  lazy val CONSUMER_GROUP: String = Try {
    config.getString("kafka.CONSUMER_GROUP")
  }.getOrElse("CONSUMER_GROUP")

  lazy val SOURCE_TOPIC: String = "SOURCE_TOPIC"
  lazy val SINK_TOPIC: String = "SINK_TOPIC"
  lazy val PARTITIONS_NUMBER: Int = Try {
    config.getString("kafka.PARTITIONS_NUMBER")
  }.map(_.toInt).getOrElse(30)
}