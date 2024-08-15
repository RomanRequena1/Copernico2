package proyectionists.no_registrales.objeto

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import consumers.no_registral.obligacion.application.entities.ObligacionesTri
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import utils.generators.Model.deliveryId
import consumers_spec.Utils.isObjetoBajaFromGetObjetoResponse
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import io.circe.parser.decode
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._

import scala.concurrent.Future
import scala.concurrent.impl.Promise


object ObjetoRSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         cassandra: MockMonitoringAndCassandraWrite)
}
abstract class ObjetoRSpec(
    getContext: ActorSystem => ObjetoRSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {
  val examples = new Examples("ObjetoSpec")

  "un objeto Tri" should "1: readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val evento = examples.objetoReadside
      val cassandra = context.cassandra
      messageProducer.produceObjetoReadside(evento)

      eventually {
        cassandra.cassandraWrite
          .cqlSelect("SELECT * FROM read_side.buc_sujeto_objeto;")
          .map { res => println(res.one().getFormattedContents)}
//          .map { res => res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)}
      }

  }

  "un objeto Tri" should "1: readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val evento = examples.objetoReadside
      val cassandra = context.cassandra
      messageProducer.produceObjetoReadside(evento)

      eventually {
        cassandra.cassandraWrite
          .cqlSelect("SELECT * FROM read_side.buc_sujeto_objeto;")
          .map { res => println(res.one().getFormattedContents)}
        //          .map { res => res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)}
      }

  }

}
