package design_principles.microservice.kafka_consumer_microservice

import akka.actor.ActorSystem
import akka.dispatchers.ActorsDispatchers
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
import akka.stream.alpakka.cassandra.CassandraMetricsRegistry
import api.stats.ClusterStats
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.context_provider.{Guardian, GuardianRequirements}
import design_principles.actor_model.mechanism.stream_supervision.MessageProcessorSupervisorActorController
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.dropwizard.DropwizardExports
import io.prometheus.client.exporter.HTTPServer
import life_cycle.AppLifecycleMicroservice

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

object MainApplication {

  def startMicroservices(
                          microservicesFactory: KafkaConsumerMicroserviceRequirements => Seq[KafkaConsumerMicroservice],
                          ip: String,
                          port: Int,
                          actorSystemName: String,
                          extraConfigurations: Config = ConfigFactory.empty
                        ): Unit = {

    val mainConfig = ConfigFactory.load()
    lazy val config = Seq(
      mainConfig,
      //TODO
      ConfigFactory parseString new ActorsDispatchers(mainConfig).actorsDispatchers,
      extraConfigurations
    ).reduce(_ withFallback _)

    implicit val system: ActorSystem = Guardian.getContext(GuardianRequirements(actorSystemName, config))
    val cassandraMetricsPort = Try(System.getenv("CASSANDRA_METRICS_PORT")).getOrElse("9089")


    try {
      new HTTPServer(cassandraMetricsPort.toInt)
      val cassandraRegistry = CassandraMetricsRegistry.get(system).getRegistry
      CollectorRegistry.defaultRegistry.register(new DropwizardExports(cassandraRegistry))
    } catch {
      case e: Exception => println(s"ERROR: Failed to export Cassandra metrics: ${e.getMessage}")
    }

    val routes = ProductionMicroserviceContextProvider.getContext(system, config) { implicit microserviceProvisioning =>
      val microservices = microservicesFactory(microserviceProvisioning)
      val userRoutes = microservices.map(_.route).reduce(_ ~ _)

      val startStopKafka = new MessageProcessorSupervisorActorController(
        microservices.flatMap { _.actorTransactionControllers }.toMap
      ).route
      val systemRoutes = (new AppLifecycleMicroservice).route
      val statRoutes = new ClusterStats().route
      userRoutes ~ systemRoutes ~ statRoutes ~ startStopKafka
    }

    AkkaHttpServer.start(routes, ip, port)(system)

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
