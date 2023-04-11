package readside.proyectionists.no_registrales.obligacion.infrastructure.main

import akka.actor.ActorSelection
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import readside.proyectionists.no_registrales.obligacion.{ObligacionAddedExencionHandler, ObligacionPersistedSnapshotHandler}

import scala.util.Try

class ObligacionProjectionistMicroservice(
    implicit m: KafkaConsumerMicroserviceRequirements
) extends KafkaConsumerMicroservice {
  val IP = Try(System.getenv("POD_NAMESPACE")).getOrElse("no")
  val PORT = Try(System.getenv("CLUSTER_PORT")).getOrElse("no")
  //"akka.tcp://actorSystemName@10.0.0.1:2552/user/actorName"
  //implicit val timescaledbActorSelector: ActorSelection = m.ctx.actorSelection("akka://PersonClassificationService/user/timescaledb")
  val timescaledbActorSelector: ActorSelection = m.ctx.actorSelection(s"akka://PersonClassificationService@${IP}:${PORT}/user/timescaledb")
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new ObligacionAddedExencionHandler,
      new ObligacionPersistedSnapshotHandler(timescaledbActorSelector)
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
