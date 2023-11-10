package config

import akka.dispatchers.ActorsDispatchers
import com.typesafe.config.ConfigFactory

object MockConfig {
  private val mainConfig = ConfigFactory.load()
  lazy val config = Seq(
    mainConfig,
    ConfigFactory parseString new ActorsDispatchers(mainConfig).actorsDispatchers
  ).reduce(_ withFallback _)
}
