package proyectionists.no_registrales.obligacion

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}

object ObligacionRSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         cassandra: MockMonitoringAndCassandraWrite)
}
abstract class ObligacionRSpec(
    getContext: ActorSystem => ObligacionRSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {
  val examples = new Examples("ObligacionRSpec")

  "Una obligacion ANT" should "1: existir en cassandra readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val evento = examples.obligacionExampleAntProjection
    val cassandra = context.cassandra
    messageProducer.produceObligacionReadside(evento)

    eventually {
      cassandra.cassandraWrite
        .cqlSelect("SELECT * FROM read_side.buc_obligaciones;")
        .map { res =>
          {
            println("Cumbia: " + res.one().getFormattedContents)
            res.one().getString("BOB_OBN_ID") should be(evento.obligacionId)
          }
        }
      //          .map { res => res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)}
    }
  }

//  "un objeto Tri" should "1: readside" in parallelActorSystemRunner { implicit s =>
//    implicit val dispatcher = s.dispatcher
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val evento = examples.objetoReadside
//    val cassandra = context.cassandra
//    messageProducer.produceObjetoReadside(evento)
//    eventually {
//      cassandra.cassandraWrite
//        .cqlSelect("SELECT * FROM read_side.buc_sujeto_objeto;")
//        .map { res =>
//          println(res.one().getFormattedContents)
//        }
//      //          .map { res => res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)}
//    }
//  }

}
