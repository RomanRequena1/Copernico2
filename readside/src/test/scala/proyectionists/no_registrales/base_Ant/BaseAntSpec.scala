package proyectionists.no_registrales.base_Ant

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import consumers.no_registral.obligacion.application.entities.ListDetallesObligaciones
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import org.scalatest.Succeeded
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import utils.generators.Model.deliveryIdAct

import java.time.LocalDateTime
import scala.concurrent.ExecutionContextExecutor

object BaseAntSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         cassandra: MockMonitoringAndCassandraWrite)
}
abstract class BaseAntSpec(
    getContext: ActorSystem => BaseAntSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {
  val examples = new Examples("ObjetoSpec")

  override protected def beforeAll(): Unit = {
    println("BEFORE ALL")
    parallelActorSystemRunner { implicit s =>
      implicit val dispatcher: ExecutionContextExecutor = s.dispatcher
      val context = getContext(s)

      context.cassandra.cassandraWrite.cqlSelect("truncate table akka.all_persistence_ids;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("truncate table akka.messages;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("truncate table akka.metadata;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("truncate table akka_snapshot.snapshots;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("truncate table akka.tag_scanning;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("truncate table akka.tag_views;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("truncate table akka.tag_write_progress;").futureValue

      context.cassandra.cassandraWrite.cqlSelect("TRUNCATE TABLE read_side.buc_obligaciones;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("TRUNCATE TABLE read_side.buc_sujeto_objeto;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("TRUNCATE TABLE read_side.buc_objeto_vinculo;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("TRUNCATE TABLE read_side.buc_sujeto;").futureValue
    }
    super.beforeAll()
  }

//  "un obligacion Ant" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
//    implicit val dispatcher = s.dispatcher
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val eventoLucas =
//      examples.obligacionAntExampleVencida.copy(BOB_SOJ_IDENTIFICADOR = "ObjetoAnt1", BOB_OBN_ID = "ObnAntPersiste")
//
//    messageProducer.produceObligacion(eventoLucas)
//
//    val cassandra = context.cassandra
//
//    eventually {
//      val resultado: AsyncResultSet = cassandra.cassandraWrite
//        .cqlSelect(
//          s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoLucas.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoLucas.BOB_SOJ_TIPO_OBJETO}';"
//        )
//        .futureValue
//      resultado.one().getString("BOB_SOJ_IDENTIFICADOR") should be(eventoLucas.BOB_SOJ_IDENTIFICADOR)
//    }
//  }

//  "un obligacion Ant con cambios" should "End to end, PCS a Readside " in parallelActorSystemRunner { implicit s =>
//    implicit val dispatcher = s.dispatcher
//
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val cassandra = context.cassandra
//
//    val eventoDiego =
//      examples.obligacionAntExampleVencida.copy(BOB_SOJ_IDENTIFICADOR = "ObjetoAntDiego2", BOB_OBN_ID = "ObnAnt2-2")
//    messageProducer.produceObligacion(eventoDiego)
//
//    val eventoDiegoModificado = eventoDiego.copy(
//      EV_ID = deliveryIdAct,
//      BOB_SALDO = 12000.50,
//      BOB_VENCIMIENTO = Some(LocalDateTime.of(2027, 12, 12, 0, 0)),
//      BOB_ESTADO = Some("PREJUDICIAL")
//    )
//
//    eventually {
//      val resultado: AsyncResultSet = cassandra.cassandraWrite
//        .cqlSelect(
//          s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiego.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiego.BOB_SOJ_TIPO_OBJETO}';"
//        )
//        .futureValue
//
//      val obligacion = resultado.one()
//      obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(eventoDiego.BOB_SOJ_IDENTIFICADOR)
//
//    } match {
//      case Succeeded => {
//        messageProducer.produceObligacion(eventoDiegoModificado)
//
//        eventually {
//          val resultado: AsyncResultSet = cassandra.cassandraWrite
//            .cqlSelect(
//              s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiegoModificado.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiegoModificado.BOB_SOJ_TIPO_OBJETO}';"
//            )
//            .futureValue
//
//          val obligacion = resultado.one()
//          obligacion.getString("BOB_ESTADO") should be(eventoDiegoModificado.BOB_ESTADO.get)
//          obligacion.getLocalDate("BOB_VENCIMIENTO").atStartOfDay() should be(eventoDiegoModificado.BOB_VENCIMIENTO.get)
//          obligacion.getFloat("BOB_SALDO") should be(eventoDiegoModificado.BOB_SALDO)
//        }
//      } match {
//        case Succeeded => {
//
//          val eventoDiegoBaja = eventoDiegoModificado.copy(
//            EV_ID = deliveryIdAct,
//            BOB_OTROS_ATRIBUTOS = Some(
//              ListDetallesObligaciones(
//                List(eventoDiegoModificado.BOB_OTROS_ATRIBUTOS.head.BOB_DETALLES.head.copy(RULE_NUMBER = Some("-1")))
//              )
//            )
//          )
//          messageProducer.produceObligacion(eventoDiegoBaja)
//
//          eventually {
//            val resultado: AsyncResultSet = cassandra.cassandraWrite
//              .cqlSelect(
//                s"SELECT count(*) FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiegoBaja.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiegoBaja.BOB_SOJ_TIPO_OBJETO}';"
//              )
//              .futureValue
//
//            val obligacion = resultado.one()
//            obligacion.getLong("count") should be(0)
//          }
//        }
//        case _ => println("Fallo Evento 2")
//      }
//      case _ => println("Fallo Evento 1")
//    }
//  }

  "Test 1: un objeto Ant" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val cassandra = context.cassandra
    val messageProducer = context.messageProducer

    val eventoRoman = examples.objetoExampleAntRoman.copy(SOJ_IDENTIFICADOR = "ObjetoAntPersiste-T1")
    messageProducer.produceObjetoAnt(eventoRoman)

    eventually {
      val resultado: AsyncResultSet = cassandra.cassandraWrite
        .cqlSelect(
          s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${eventoRoman.SOJ_SUJ_IDENTIFICADOR}';"
        )
        .futureValue

      resultado.one().getString("SOJ_SUJ_IDENTIFICADOR") should be(eventoRoman.SOJ_SUJ_IDENTIFICADOR)
    }
  }

  "Test 2: un objeto Ant con cambios" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val eventoRoman = examples.objetoAntExample.copy(SOJ_SUJ_IDENTIFICADOR = "CuitAnt-T2",SOJ_IDENTIFICADOR = "ObjetoAntCc-T2")
    messageProducer.produceObjetoAnt(eventoRoman)

    eventually {
      val resultado: AsyncResultSet = cassandra.cassandraWrite
        .cqlSelect(
          s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${eventoRoman.SOJ_SUJ_IDENTIFICADOR}';"
        )
        .futureValue
      resultado.one().getString("SOJ_SUJ_IDENTIFICADOR") should be(eventoRoman.SOJ_SUJ_IDENTIFICADOR)
    } match {
      case Succeeded => {

        val eventoRomanModificado = eventoRoman.copy(
          EV_ID = deliveryIdAct,
          SOJ_DESCRIPCION = Some("Nueva Descripcion"),
          SOJ_FECHA_INICIO = Some(LocalDateTime.of(2020, 12, 12, 0, 0)),
          SOJ_BASE_IMPONIBLE = Some(12345.5)
        )
        messageProducer.produceObjetoAnt(eventoRomanModificado)
        eventually {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${eventoRoman.SOJ_SUJ_IDENTIFICADOR}';"
            )
            .futureValue
          val objeto = resultado.one()

          objeto.getString("SOJ_DESCRIPCION") should be(eventoRomanModificado.SOJ_DESCRIPCION.get)
          objeto.getLocalDate("SOJ_FECHA_INICIO").atStartOfDay() should be(eventoRomanModificado.SOJ_FECHA_INICIO.get)
          objeto.getFloat("SOJ_BASE_IMPONIBLE") should be(eventoRomanModificado.SOJ_BASE_IMPONIBLE.get)

        } match {
          case Succeeded => {

            val eventoRomanEliminado = eventoRomanModificado.copy(
              EV_ID = deliveryIdAct,
              SOJ_ESTADO = Some("BAJA")
            )
            messageProducer.produceObjetoAnt(eventoRomanEliminado)

            eventually {
              val resultado: AsyncResultSet = cassandra.cassandraWrite
                .cqlSelect(
                  s"SELECT count(*) FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${eventoRoman.SOJ_SUJ_IDENTIFICADOR}';"
                )
                .futureValue
              val objeto = resultado.one()
              objeto.getLong("count") should be(0)
            }
          }
          case _ => println("Fallo Delete")
        }
      }
      case _ => println("FALLO")
    }
  }

  "Test 3: una obligacion Ant" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val eventoLucas =
      examples.obligacionAntExampleVencida.copy(BOB_SUJ_IDENTIFICADOR = "CuitAnt-T3",BOB_SOJ_IDENTIFICADOR = "ObjetoAnt-T3", BOB_OBN_ID = "ObnAntPersiste-T3")

    messageProducer.produceObligacion(eventoLucas)

    val cassandra = context.cassandra

    eventually {
      val resultado: AsyncResultSet = cassandra.cassandraWrite
        .cqlSelect(
          s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoLucas.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoLucas.BOB_SOJ_TIPO_OBJETO}';"
        )
        .futureValue
      resultado.one().getString("BOB_SOJ_IDENTIFICADOR") should be(eventoLucas.BOB_SOJ_IDENTIFICADOR)
    }
  }

  "Test 5: una obligacion Ant con cambios" should "End to end, PCS a Readside " in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher

    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val eventoDiego =
      examples.obligacionAntExampleVencida.copy(BOB_SUJ_IDENTIFICADOR = "CuitAnt-T4",BOB_SOJ_IDENTIFICADOR = "ObjetoAnt-T4", BOB_OBN_ID = "ObnAnt-T4")
    //cassandra.cassandraWrite.cqlSelect(s"TRUNCATE read_side.buc_obligaciones;")

    messageProducer.produceObligacion(eventoDiego)

    val eventoDiegoModificado = eventoDiego.copy(
      EV_ID = deliveryIdAct,
      BOB_SALDO = 40000.50,
      BOB_VENCIMIENTO = Some(LocalDateTime.of(2027, 12, 12, 0, 0)),
      BOB_ESTADO = Some("PREJUDICIAL")
    )

    eventually {
      val resultado: AsyncResultSet = cassandra.cassandraWrite
        .cqlSelect(
          s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiego.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiego.BOB_SOJ_TIPO_OBJETO}';"
        )
        .futureValue

      val obligacion = resultado.one()
      obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(eventoDiego.BOB_SOJ_IDENTIFICADOR)

    } match {
      case Succeeded => {
        messageProducer.produceObligacion(eventoDiegoModificado)

        eventually {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiegoModificado.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiegoModificado.BOB_SOJ_TIPO_OBJETO}';"
            )
            .futureValue

          val obligacion = resultado.one()
          obligacion.getString("BOB_ESTADO") should be(eventoDiegoModificado.BOB_ESTADO.get)
          obligacion.getLocalDate("BOB_VENCIMIENTO").atStartOfDay() should be(eventoDiegoModificado.BOB_VENCIMIENTO.get)
          obligacion.getFloat("BOB_SALDO") should be(eventoDiegoModificado.BOB_SALDO)
        }
      } match {
        case Succeeded => {
          val eventoDiegoBaja = eventoDiegoModificado.copy(
            EV_ID = deliveryIdAct,
            BOB_OTROS_ATRIBUTOS = Some(
              ListDetallesObligaciones(
                List(eventoDiegoModificado.BOB_OTROS_ATRIBUTOS.head.BOB_DETALLES.head.copy(RULE_NUMBER = Some("-1")))
              )
            )
          )

          messageProducer.produceObligacion(eventoDiegoBaja)

          eventually {
            val resultado: AsyncResultSet = cassandra.cassandraWrite
              .cqlSelect(
                s"SELECT count(*) FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${eventoDiegoBaja.BOB_SOJ_IDENTIFICADOR}' AND BOB_SOJ_TIPO_OBJETO = '${eventoDiegoBaja.BOB_SOJ_TIPO_OBJETO}';"
              )
              .futureValue

            val obligacion = resultado.one()
            obligacion.getLong("count") should be(0)
          }
        }
        case _ => println("Fallo Evento 2")
      }
      case _ => println("Fallo Evento 1")
    }
  }
}
