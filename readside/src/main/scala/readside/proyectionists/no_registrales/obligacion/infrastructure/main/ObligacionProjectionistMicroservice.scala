package readside.proyectionists.no_registrales.obligacion.infrastructure.main

import akka.actor.{ActorRef, ActorSelection, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import readside.proyectionists.no_registrales.obligacion.{ObligacionAddedExencionHandler, ObligacionPersistedSnapshotHandler}
import timescaledb.{Connec, TimesActor}

import scala.util.Try

class ObligacionProjectionistMicroservice(
    implicit m: KafkaConsumerMicroserviceRequirements
) extends KafkaConsumerMicroservice {
  val IP = Try(System.getenv("POD_NAMESPACE")).getOrElse("no")
  val PORT = Try(System.getenv("CLUSTER_PORT")).getOrElse("no")
  //"akka.tcp://actorSystemName@10.0.0.1:2552/user/actorName"
  //implicit val timescaledbActorSelector: ActorSelection = m.ctx.actorSelection("akka://PersonClassificationService/user/timescaledb")
  //val timescaledbActorSelector: ActorSelection = m.ctx.actorSelection("akka://PersonClassificationService@$pcs-cop-desa:2551/user/timescaledb")
  val timescaledbActor: ActorRef = m.ctx.actorOf(Props[TimesActor](), "timescaledbread")
  //println("CUMBIA " + timescaledbActor.path)
  timescaledbActor ! Connec
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new ObligacionAddedExencionHandler,
      new ObligacionPersistedSnapshotHandler(timescaledbActor)
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
