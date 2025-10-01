package kafka

import scala.util.Try

import com.typesafe.config.Config

class KafkaConfig(config: Config) {
  lazy val KAFKA_BROKER: String = Try { config.getString("kafka.brokers") }.getOrElse("0.0.0.0:9092")
  lazy val KAFKA_PSRM_BROKER: String = Try {
    config.getString("kafka.psrm.brokers")
  }.getOrElse(sys.env.getOrElse("KAFKA_BROKERS_LIST_PSRM", "0.0.0.0:9092"))

  lazy val CONSUMER_GROUP: String = Try {
    config.getString("kafka.CONSUMER_GROUP")
  }.getOrElse("CONSUMER_GROUP")
  lazy val SOURCE_TOPIC: String = "SOURCE_TOPIC"
  lazy val SINK_TOPIC: String = "SINK_TOPIC"
  lazy val PARTITIONS_NUMBER: Int = Try { config.getString("kafka.PARTITIONS_NUMBER") }.map(_.toInt).getOrElse(30)

}
