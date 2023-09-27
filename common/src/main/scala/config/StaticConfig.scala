package config

import akka.dispatchers.ActorsDispatchers
import com.typesafe.config.ConfigFactory

object StaticConfig {
  private val mainConfig = ConfigFactory.load()
  lazy val config = Seq(
    mainConfig,
    //TODO  verificar
    ConfigFactory parseString new ActorsDispatchers(mainConfig).actorsDispatchers
    //ConfigFactory parseString StrongScaling.apply(mainConfig).strongScalingDispatcherCassandra
  ).reduce(_ withFallback _)
}
