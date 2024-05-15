package consumers.no_registral.obligacion.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.obligacion.infrastructure.consumer._
import consumers.no_registral.obligacion.infrastructure.http.ObligacionStateAPI
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import org.camunda.dmn.DmnEngine
import org.camunda.dmn.parser.ParsedDmn
import scalaz.\/
import scalaz.concurrent.Task.Try

import java.io.FileInputStream

class ObligacionMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {

  implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoringAndMessageProducer)
  ObjetoVinculoActor.startWithRequirements(monitoringAndMessageProducer)

  //println("HOLA UNA VEZ? CUMBIA")




  //private val log = LoggerFactory.getLogger(this.getClass)
  //val timescaledbActor: ActorRef = m.ctx.actorOf(Props[TimesActor](), "timescaledb")
  //log.error("CUMBIA " + timescaledbActor.path)
  //timescaledbActor ! Connec
  //val obj = new Timescaledb(timescaledbActor)
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ObligacionTributariaTransaction(actor, monitoring),
      ObligacionTributariaRetryTransaction(actor, monitoring),
      ObligacionTributariaTransactionBilletera(actor, monitoring),
      ObligacionTributariaTransactionInmueble(actor, monitoring),
      ObligacionTributariaTransactionAutomotor(actor, monitoring),
      ObligacionTributariaTransactionIngresoBruto(actor, monitoring),
      ObligacionTributariaTransactionMultiobjeto(actor, monitoring),
      ObligacionTributariaTransactionEmbarcacion(actor, monitoring),
      ObligacionTributariaTransactionCuotaPlan(actor, monitoring),
      ObligacionTributariaTransactionJuicio(actor, monitoring),
      ObligacionTributariaTransaction2(actor, monitoring),
      ObligacionTributariaTransaction3(actor, monitoring),
      ObligacionNoTributariaTransaction(actor, monitoring)
    )

  def route: Route = {
    (Seq(
      ObligacionStateAPI(actor, monitoring).route
    ) ++
    actorTransactions.map(_.route).toSeq) reduce (_ ~ _)
  }

}


