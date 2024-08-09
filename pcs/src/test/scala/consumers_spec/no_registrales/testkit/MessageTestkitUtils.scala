package consumers_spec.no_registrales.testkit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.ConfigFactory
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.infrastructure.consumer._
import consumers.no_registral.obligacion.application.entities.{ObligacionExternalDto, ObligacionesAnt, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.consumer.ObligacionTributariaTransaction
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.infrastructure.consumer.SujetoTributarioTransaction
import design_principles.actor_model.Response
import design_principles.external_pub_sub.kafka.KafkaMock.MessageProcessorImplicits
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{MessageProcessor, MessageProducer}
import monitoring.DummyMonitoring
import io.circe.syntax.EncoderOps
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits.SujetoExternalDtoEncoder
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.ObjetosTriEncoder
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.ObligacionExternalDtoEncoder
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.ObligacionesTriEncoder


class MessageTestkitUtils(sujeto: ActorRef) {
  implicit val actorTransactionRequirements: ActorTransactionRequirements = ActorTransactionRequirements(
    executionContext = scala.concurrent.ExecutionContext.Implicits.global,
    config = ConfigFactory.empty
  )
  implicit class StartMessageProcessor(messageBroker: MessageProcessor with MessageProducer) {
    val monitoring = new DummyMonitoring
    def startProcessing(topics: Set[ActorTransaction[_]] = Set.empty): Unit = {

      (if (topics.isEmpty)
         Set(
           ObjetoTributarioTransaction(sujeto, monitoring),
           ObligacionTributariaTransaction(sujeto, monitoring),
           SujetoTributarioTransaction(sujeto, monitoring),
         ) // if no filter is set, then allow passthrough
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
    def produceObligacion(obligacion: ObligacionesTri): Future[akka.Done] = {
      def topic = "DGR-COP-OBLIGACIONES-TRI"
      println("Pr OBN : " + obligacion.asJson)
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
