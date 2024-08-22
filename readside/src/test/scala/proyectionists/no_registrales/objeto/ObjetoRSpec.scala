package proyectionists.no_registrales.objeto

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.internal.core.cql.DefaultRow
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
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.Future
import scala.concurrent.impl.Promise
import scala.util.{Failure, Success}


object ObjetoRSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         cassandra: MockMonitoringAndCassandraWrite,
                        )
}
abstract class ObjetoRSpec(
    getContext: ActorSystem => ObjetoRSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {
  val examples = new Examples("ObjetoSpec")

//  "un sujeto" should "End to end, PCS a Readside" in parallelActorSystemRunner {
//    implicit s =>
//      implicit val dispatcher = s.dispatcher
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val eventoDiego = examples.sujetoExampleDiego
//
//      messageProducer.produceSujeto(eventoDiego)
//
//      val cassandra = context.cassandra
//
//      eventually {
//        val resultado: AsyncResultSet = cassandra.cassandraWrite
//          .cqlSelect(s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${eventoDiego.SUJ_IDENTIFICADOR}';").futureValue
//        resultado.one().getString("SUJ_IDENTIFICADOR") should be(eventoDiego.SUJ_IDENTIFICADOR)
//      }
//  }

  "un objeto" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val eventoDiego = examples.objetoExampleDiego

      messageProducer.produceObjeto(eventoDiego)

      val cassandra = context.cassandra

      eventually {
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${eventoDiego.SOJ_SUJ_IDENTIFICADOR}';").futureValue
        resultado.one().getString("SOJ_SUJ_IDENTIFICADOR") should be(eventoDiego.SOJ_SUJ_IDENTIFICADOR)
      }
  }

  "un obligacion" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val eventoDiego = examples.obligacionExampleDiego

      messageProducer.produceObligacion(eventoDiego)

      val cassandra = context.cassandra

      eventually {
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiego.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiego.BOB_SOJ_TIPO_OBJETO}';").futureValue
        resultado.one().getString("BOB_SOJ_IDENTIFICADOR") should be(eventoDiego.BOB_SOJ_IDENTIFICADOR)
      }
  }

    //  "un objeto" should "1: readside" in parallelActorSystemRunner {
//    implicit s =>
//      implicit val dispatcher = s.dispatcher
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val evento = examples.objetoReadside
//      val cassandra = context.cassandra
//      messageProducer.produceObjetoReadside(evento)
//
//      eventually {
//        cassandra.cassandraWrite
//          .cqlSelect("SELECT * FROM read_side.buc_sujeto_objeto;")
////          .map { res => println(res.one().getFormattedContents)}
//          .map { res => {
//            println(s"Cuit a consultar: ${evento.objetoId} | Respuesta de Cassandra: ${res.one().getString("soj_identificador")}")
//            res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)
//          }}
//      }
//  }



  /*"un objeto" should "2: readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val eventoDiego = examples.objetoExampleDiego

      messageProducer.produceObjeto(eventoDiego)

      val evento = examples.objetoReadside
      val cassandra = context.cassandra
      val messageProcessor = context.messageProcessor

      eventually {
        println("aaa")
        val a: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect("SELECT * FROM read_side.buc_sujeto_objeto;").futureValue
        a.one().getString("SUJ_IDENTIFICADOR") should be("aa")
      }*/

//      cassandra.cassandraWrite
//        .cqlSelect("SELECT * FROM read_side.buc_sujeto_objeto;").andThen {
//          case Failure(exception) => println("error")
//          case Success(value) => {
////            println("ttoooo " + value.one())
////            if (value.one() != null) {
////              println("opa no null")
////            } else {
////              println("NULLL")
////            }
//          }
//        }
//      messageProcessor.run()
      //messageProducer.consumirObjetoReadside(evento)
//      PatienceConfig(timeout: Span = scaled(Span(150, Millis)), interval: Span = scaled(Span(15, Millis)))
      //implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(10, Seconds)), scaled(Span(1, Millis)))

//          .andThen {
//            case Failure(exception) => println("error")
//            case Success(value) => {
////              println("tt" + value)
//              if (value.one() != null) {
//                println("opa no null")
//              } else {
//                println("NULLL")
//                "" should be(evento.registro.get.SOJ_IDENTIFICADOR)
//              }
//            }
//          }
//          .onComplete { res => {
//            res match {
//              case Failure(exception) => println(exception)
//              case Success(res) => {
//                println("tt "+ res)
//                if (res != null) {
//                  res.map(fila => {
//                    fila match {
//                      case row: DefaultRow => println("c1")
//                      case row: ReactiveRow => println("c2")
//                      case _ => println("c3")
//                    }
//                    println(s"Cuit a consultar: ${evento.objetoId} | Respuesta de Cassandra: ${fila.getString("soj_identificador")}")
//                  })
//                } else {
//                  println("Vacioooooo")
//
//                }
////                res.map(fila => println(s"Cuit a consultar: ${evento.objetoId} | Respuesta de Cassandra: ${fila.getString("soj_identificador")}"))
//              }
//            }
//          }}
//            println(s"Cuit a consultar: ${evento.objetoId} | Respuesta de Cassandra: ${res.one().getString("soj_identificador")}")
//            res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)
//            }

          //          .map { res => println(res.one().getFormattedContents)}
//          .map { res => {
//            println(s"Cuit a consultar: ${evento.objetoId} | Respuesta de Cassandra: ${res.one().getString("soj_identificador")}")
//            res.one().getString("soj_identificador") should be(evento.registro.get.SOJ_IDENTIFICADOR)
//             }
//          }



}
