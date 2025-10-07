package kafka

import scala.util.Try
import com.typesafe.config.Config

class KafkaConfig(config: Config) {

  lazy val KAFKA_BROKER: String = {
    val broker = Try { config.getString("kafka.brokers") }.getOrElse("0.0.0.0:9092")
    println(s"[KAFKA_CONFIG] ✅ KAFKA_BROKER configurado: $broker")
    broker
  }

  lazy val PSRM_ENABLED: Boolean = {
    val enabled = Try { config.getBoolean("kafka.psrm.enabled") }.getOrElse(false)
    println(s"[KAFKA_CONFIG] 🔍 PSRM_ENABLED: $enabled")
    enabled
  }

  lazy val KAFKA_PSRM_BROKER: Option[String] = {
    val envVar = sys.env.get("KAFKA_BROKERS_LIST_PSRM")
    val fromConfig = Try { config.getString("kafka.psrm.brokers") }.toOption

    val broker = fromConfig.orElse(envVar)

    println(s"========================================")
    println(s"[KAFKA_CONFIG] 🔍 Configuración PSRM:")
    println(s"[KAFKA_CONFIG]    - Habilitado: $PSRM_ENABLED")
    println(s"[KAFKA_CONFIG]    - Desde config: $fromConfig")
    println(s"[KAFKA_CONFIG]    - Desde env var: $envVar")
    println(s"[KAFKA_CONFIG]    - Valor final: ${broker.getOrElse("NO CONFIGURADO")}")

    if (PSRM_ENABLED && broker.isEmpty) {
      println(s"[KAFKA_CONFIG] ⚠️ ADVERTENCIA: PSRM habilitado pero sin broker configurado!")
    }

    if (broker.exists(b => b == "0.0.0.0:9092" || b.isEmpty)) {
      println(s"[KAFKA_CONFIG] ⚠️ ADVERTENCIA: Broker PSRM tiene valor inválido")
      println(s"========================================")
      None
    } else {
      println(s"========================================")
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