package proyectionists.no_registrales.testkit

import akka.actor.{ActorRef, ActorSystem}
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.MockMonitoringAndCassandraWrite
import com.typesafe.config.ConfigFactory
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.consumer._
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.{ObjetoSnapshotPersistedEncoder, ObjetosTriEncoder}
import consumers.no_registral.obligacion.application.entities.ObligacionesTri
import consumers.no_registral.obligacion.infrastructure.consumer.ObligacionTributariaTransaction
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.ObligacionesTriEncoder
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.infrastructure.consumer.SujetoTributarioTransaction
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits.SujetoExternalDtoEncoder
import design_principles.external_pub_sub.kafka.KafkaProduction
import design_principles.projection.mock.CassandraWriteMock
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{MessageProcessor, MessageProducer}
import monitoring.DummyMonitoring
import readside.proyectionists.no_registrales.objeto.ObjetoSnapshotPersistedHandler
import readside.proyectionists.no_registrales.obligacion.ObligacionPersistedSnapshotHandler
import readside.proyectionists.no_registrales.sujeto.SujetoSnapshotPersistedHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//import design_principles.external_pub_sub.kafka.KafkaMock.MessageProcessorImplicits
import design_principles.external_pub_sub.kafka.KafkaProduction.MessageProcessorImplicits

class MessageTestkitUtils2(s: ActorSystem) {
//  implicit val actorTransactionRequirements: ActorTransactionRequirements = ActorTransactionRequirements(
//    executionContext = scala.concurrent.ExecutionContext.Implicits.global,
//    config = ConfigFactory.empty
//  )
//  val monitoring = new KamonMonitoring
//  val executionContext = scala.concurrent.ExecutionContext.Implicits.global
//  val rebalancerListener: ActorRef =
//    s.actorOf(
//      Props(
//        new TopicListener(
//          typeKeyName = "rebalancerListener",
//          monitoring
//        )
//      )
//    )
//
//  implicit val kafkaConsumerMicroserviceRequirements = KafkaConsumerMicroserviceRequirements(
//    monitoring = monitoring,
//    ctx = s,
//    queryStateApiRequirements = QueryStateApiRequirements(s, scala.concurrent.ExecutionContext.Implicits.global),
//    actorTransactionRequirements = actorTransactionRequirements,
//    kafkaMessageProcessorRequirements = KafkaMessageProcessorRequirements.productionSettings(rebalancerListener, monitoring, s, executionContext),
//    config = ConfigFactory.empty(),
//    cassandraWrite = new CassandraWriteProduction(),
//  )
  //implicit val mockMonitoringAndCassandraWrite = MockMonitoringAndCassandraWrite.apply(new CassandraWriteMock())

  implicit class StartMessageProcessor(messageBroker: MessageProcessor with MessageProducer) {
    val monitoring = new DummyMonitoring
    def startProcessing(topics: Set[ActorTransaction[_]] = Set.empty): Unit = {
      implicit val actorTransactionRequirements: ActorTransactionRequirements = ActorTransactionRequirements(
        executionContext = s.getDispatcher,
        config = ConfigFactory.empty
      )
      implicit val mockMonitoringAndCassandraWrite = MockMonitoringAndCassandraWrite(monitoring, new CassandraWriteMock(), actorTransactionRequirements)

      (if (topics.isEmpty) {
        Set(
          new ObjetoSnapshotPersistedHandler,
          new SujetoSnapshotPersistedHandler,
          new ObligacionPersistedSnapshotHandler
        )
      } // if no filter is set, then allow passthrough
       else topics)
        .foreach { transaction =>
          messageBroker.createTopic(transaction.topic)
          messageBroker.subscribeActorTransaction(
            transaction.topic,
            transaction
          )
        }

    }
  }

}

object MessageTestkitUtils2 {
  implicit class MessageProducerNoRegistrales(messageProducer: MessageProducer) {

    def produceObjetoReadside(objeto: ObjetoSnapshotPersisted): Future[akka.Done] = {
      def topic = "ObjetoSnapshotPersistedReadside"

//      println("Pr Obj Reads : " + objeto.asJson)
      messageProducer.produce(
        Seq(
          KafkaKeyValue(
            aggregateRoot = "1",
            json = objeto.asJson.toString()
          )
        ),
        topic
      )(_ => ())
    }

    def consumirObjetoReadside(objeto: ObjetoSnapshotPersisted) = {
      def topic = "ObjetoSnapshotPersistedReadside"

      messageProducer match {
        case production: KafkaProduction => {
          val a = production.PubSub.Message(topic = topic, message = KafkaKeyValue("1", objeto.asJson.toString()))
          production.receive(a)
        }
        case _ => println("?")
      }

    }


    def produceObligacion(obligacion: ObligacionesTri): Future[akka.Done] = {
      def topic = "DGR-COP-OBLIGACIONES-TRI"
      messageProducer.produce(
        Seq(
          KafkaKeyValue(
            aggregateRoot =
              s"Sujeto-${obligacion.BOB_SUJ_IDENTIFICADOR}-Objeto-${obligacion.BOB_SOJ_IDENTIFICADOR}-Tipo-I-Obligacion-${obligacion.BOB_OBN_ID}",
            json = obligacion.asJson.toString()
          )
        ),
        topic
      )(_ => ())
    }

    def produceObjeto(objeto: ObjetoExternalDto.ObjetosTri): Future[akka.Done] = {
      val topic = "DGR-COP-OBJETOS-TRI"

      messageProducer.produce(
        Seq(
          KafkaKeyValue(
            aggregateRoot = s"Sujeto-${objeto.SOJ_SUJ_IDENTIFICADOR}-Objeto-${objeto.SOJ_IDENTIFICADOR}-Tipo-I",
            json = objeto.asJson.toString()
          )
        ),
        topic
      )(_ => ())
    }

    def produceSujeto(sujeto: SujetoExternalDto): Future[akka.Done] = {
      def topic = sujeto match {
        case _: SujetoExternalDto.SujetoAnt => "DGR-COP-SUJETO-ANT"
        case _: SujetoExternalDto.SujetoTri => "DGR-COP-SUJETO-TRI"
      }
      messageProducer.produce(Seq(
                                KafkaKeyValue(
                                  aggregateRoot = s"Sujeto-${sujeto.SUJ_IDENTIFICADOR}",
                                  json = sujeto.asJson.toString()
                                )
                              ),
                              topic)(_ => ())
    }
  }
}
