package proyectionists.no_registrales.base_Tri

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ListDetallesObligaciones}
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import org.scalatest.{FixtureContext, Succeeded}
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import utils.generators.Model.deliveryIdAct

import java.time.LocalDateTime
import scala.concurrent.ExecutionContextExecutor

object BaseTriSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         cassandra: MockMonitoringAndCassandraWrite)
}
abstract class BaseTriSpec(
    getContext: ActorSystem => BaseTriSpec.TestContext
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
      context.cassandra.cassandraWrite.cqlSelect("TRUNCATE TABLE read_side.buc_sujeto;").futureValue
      context.cassandra.cassandraWrite.cqlSelect("TRUNCATE TABLE read_side.buc_objeto_vinculo;").futureValue
    }
    super.beforeAll()
  }

  "Test 1: un sujeto" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val eventoRomanBase = examples.sujetoExampleRoman.copy(SUJ_IDENTIFICADOR = "SujetoPersiste-T1")

    messageProducer.produceSujeto(eventoRomanBase)

    val cassandra = context.cassandra

    eventually {
      val resultado: AsyncResultSet = cassandra.cassandraWrite
        .cqlSelect(
          s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${eventoRomanBase.SUJ_IDENTIFICADOR}';"
        )
        .futureValue
      resultado.one().getString("SUJ_IDENTIFICADOR") should be(eventoRomanBase.SUJ_IDENTIFICADOR)
    } match {
      case Succeeded => {
        val eventoRomanBaseModificado = eventoRomanBase.copy(
          EV_ID = deliveryIdAct,
          SUJ_TELEFONO = Some("3515591844"),
          SUJ_EMAIL = Some("roman.requena@peperina.io"),
          SUJ_DIRECCION = Some("Av. Arturo Capdevila 959")
        )
        messageProducer.produceSujeto(eventoRomanBaseModificado)

        eventually {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${eventoRomanBase.SUJ_IDENTIFICADOR}';"
            )
            .futureValue
          val sujeto = resultado.one()

          sujeto.getString("SUJ_TELEFONO") should be(eventoRomanBaseModificado.SUJ_TELEFONO.get)
          sujeto.getString("SUJ_EMAIL") should be(eventoRomanBaseModificado.SUJ_EMAIL.get)
          sujeto.getString("SUJ_DIRECCION") should be(eventoRomanBaseModificado.SUJ_DIRECCION.get)
        }
      }
      case _ => println("ERROR")
    }
  }

  "Test 2: un objeto Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val eventoRomanTri = examples.objetoExampleRoman.copy(SOJ_SUJ_IDENTIFICADOR = "CuitTri-T2",SOJ_IDENTIFICADOR = "ObjetoTriPersiste-T2")

    messageProducer.produceObjeto(eventoRomanTri)

    val cassandra = context.cassandra

    eventually {
      val resultado: AsyncResultSet = cassandra.cassandraWrite
        .cqlSelect(
          s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${eventoRomanTri.SOJ_SUJ_IDENTIFICADOR}';"
        )
        .futureValue
      resultado.one().getString("SOJ_SUJ_IDENTIFICADOR") should be(eventoRomanTri.SOJ_SUJ_IDENTIFICADOR)
    }
  }
  "Test 3: un objeto Tri con cambios" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val eventoRoman = examples.objetoExampleRoman.copy(EV_ID = deliveryIdAct,SOJ_SUJ_IDENTIFICADOR = "CuitTriCc-T3", SOJ_IDENTIFICADOR = "ObjetoTri-T3")

    messageProducer.produceObjeto(eventoRoman)
    val cassandra = context.cassandra

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
        messageProducer.produceObjeto(eventoRomanModificado)
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
            messageProducer.produceObjeto(eventoRomanEliminado)

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

  //Controlar persistencia de la obn
  //TODO Controlar que se modifiquen datos de la obn
  //TODO Controlar que se modifiquen mas datos de la obn
  //Controlar baja de la obn

  "Test 4: un obligacion" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val eventoLucas =
      examples.obligacionExampleVencidaLucas.copy(BOB_SUJ_IDENTIFICADOR = "CuitTri-T4",BOB_SOJ_IDENTIFICADOR = "ObjetoTri-T4", BOB_OBN_ID = "ObnTriPersiste-T4")

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

  "Test 5: un obligacion con cambios" should "End to end, PCS a Readside " in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher

    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val eventoDiego =
      examples.obligacionExampleDiego.copy(BOB_SUJ_IDENTIFICADOR = "CuitTri-T5",BOB_SOJ_IDENTIFICADOR = "ObjetoTri-T5", BOB_OBN_ID = "ObnTri-T5")
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
