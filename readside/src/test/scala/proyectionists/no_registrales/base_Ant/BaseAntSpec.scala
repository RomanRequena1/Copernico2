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

  "un obligacion Ant" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val eventoLucas =
      examples.obligacionAntExampleVencida.copy(BOB_SOJ_IDENTIFICADOR = "ObjetoAnt1", BOB_OBN_ID = "ObnAntPersiste")

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

  //Controlar persistencia de la obn
  //TODO Controlar que se modifiquen datos de la obn
  //TODO Controlar que se modifiquen mas datos de la obn
  //Controlar baja de la obn

  "un obligacion Ant con cambios" should "End to end, PCS a Readside " in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher

    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val eventoDiego =
      examples.obligacionAntExampleVencida.copy(BOB_SOJ_IDENTIFICADOR = "ObjetoAnt2", BOB_OBN_ID = "ObnAnt2-2")
    messageProducer.produceObligacion(eventoDiego)

    val eventoDiegoModificado = eventoDiego.copy(
      EV_ID = deliveryIdAct,
      BOB_SALDO = 12000.50,
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
          println("EventoBaja: ")

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
