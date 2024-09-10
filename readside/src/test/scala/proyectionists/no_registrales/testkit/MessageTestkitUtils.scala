package proyectionists.no_registrales.testkit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.actor.{ActorRef, ActorSystem}
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.MockMonitoringAndCassandraWrite
import com.typesafe.config.ConfigFactory
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.consumer._
import consumers.no_registral.obligacion.application.entities.{ObligacionesAnt, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.consumer.{ObligacionNoTributariaTransaction, ObligacionTributariaTransaction}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.infrastructure.consumer.SujetoTributarioTransaction
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{MessageProcessor, MessageProducer}
import monitoring.DummyMonitoring
import io.circe.syntax.EncoderOps
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits.SujetoTriEncoder
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.{ObjetoSnapshotPersistedEncoder, ObjetosAntEncoder, ObjetosExternalDtoEncoder, ObjetosTriEncoder}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.{ObligacionesAntEncoder, ObligacionesTriEncoder}
import design_principles.external_pub_sub.kafka.{KafkaMock, KafkaProduction}
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.no_registrales.objeto.ObjetoSnapshotPersistedHandler
import readside.proyectionists.no_registrales.obligacion.ObligacionPersistedSnapshotHandler
import readside.proyectionists.no_registrales.sujeto.SujetoSnapshotPersistedHandler
import design_principles.external_pub_sub.kafka.KafkaProduction.MessageProcessorImplicits

class MessageTestkitUtils(s: ActorSystem, sujeto: ActorRef) {

  implicit class StartMessageProcessor(messageBroker: MessageProcessor with MessageProducer) {
    val monitoring = new DummyMonitoring
    def startProcessing(topics: Set[ActorTransaction[_]] = Set.empty): Unit = {
      implicit val actorTransactionRequirements: ActorTransactionRequirements = ActorTransactionRequirements(
        executionContext = s.getDispatcher,
        config = ConfigFactory.empty
      )
      implicit val mockMonitoringAndCassandraWrite =
        MockMonitoringAndCassandraWrite(monitoring, new CassandraWriteMock(), actorTransactionRequirements)
      (if (topics.isEmpty) {
         Set(
           new ObjetoSnapshotPersistedHandler,
           new SujetoSnapshotPersistedHandler,
           new ObligacionPersistedSnapshotHandler,
           ObjetoTributarioTransaction(sujeto, monitoring),
           ObjetoNoTributarioTransaction(sujeto, monitoring),
           ObligacionTributariaTransaction(sujeto, monitoring),
           ObligacionNoTributariaTransaction(sujeto, monitoring),
           SujetoTributarioTransaction(sujeto, monitoring)
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

object MessageTestkitUtils {
  implicit class MessageProducerNoRegistrales(messageProducer: MessageProducer) {
    import consumers_spec.no_registrales.testsuite.ToJson._

    def produceObjetoReadside(objeto: ObjetoSnapshotPersisted): Future[akka.Done] = {
      def topic = "ObjetoSnapshotPersistedReadside"

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

    def produceObligacion(obligacion: ObligacionesAnt): Future[akka.Done] = {
      def topic = "DGR-COP-OBLIGACIONES-ANT"
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

    def produceObjetoAnt(objeto: ObjetoExternalDto.ObjetosAnt): Future[akka.Done] = {

      val topic = "DGR-COP-OBJETOS-ANT"

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



    def produceSujeto(sujeto: SujetoExternalDto.SujetoTri): Future[akka.Done] = {
      def topic = "DGR-COP-SUJETO-TRI"
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
