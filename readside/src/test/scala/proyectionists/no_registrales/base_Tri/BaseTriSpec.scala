package proyectionists.no_registrales.base_Tri

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ObjetosTri}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.{DetallesObjetoDecoder, ObjetosTriDecoder}
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.{
  DetallesObligacionDecoder,
  ObligacionesTriDecoder
}
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import io.circe.parser.decode
import kafka.{MessageProcessor, MessageProducer}
import org.scalatest.Assertion
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import utils.generators.Model.deliveryIdAct

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

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

  case class TestData(
      sujetoId: String,
      objetoId: String,
      objetoTipo: String,
      obnId: String
  )

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

  "Test 1 Alta/Modificacion: de sujeto Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData = TestData(
        sujetoId = "CuitTri_T1",
        objetoId = "ObjetoTri_T1",
        objetoTipo = "A",
        obnId = "Obn_T1"
      )

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = LocalDateTime.parse("2024-11-13T00:00")
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${testData.sujetoId}';"
          )
          .futureValue

        val sujeto = resultado.one()

        // Persistir el mail y denominacion
        sujeto.getString("SUJ_IDENTIFICADOR") should be(testData.sujetoId)
        sujeto.getString("SUJ_DENOMINACION") should be("Sujeto-Test1")
        sujeto.getString("SUJ_EMAIL") should be("test1@ejemplo.com")

        // No persistir direccion ni saldo
        sujeto.getInt("SUJ_CAT_SUJ_ID") should be(0)
        sujeto.isNull("SUJ_DIRECCION") should be(true)
      }

      def verifyCassandra2(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = LocalDateTime.parse("2024-11-13T00:00")
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${testData.sujetoId}';"
          )
          .futureValue

        val sujeto = resultado.one()

        //Mantener la denominacion
        sujeto.getString("SUJ_DENOMINACION") should be("Sujeto-Test1")

        //Persistir nueva direccion y canal origen
        sujeto.getString("SUJ_IDENTIFICADOR") should be(testData.sujetoId)
        sujeto.getString("SUJ_DIRECCION") should be("Calle falsa 123")
        sujeto.getString("SUJ_CANAL_ORIGEN") should be("OTAX")

        // Eliminar el email
        sujeto.isNull("SUJ_EMAIL") should be(true)
      }

      /*
     El evento debe:
    - Persistir el mail y denominacion
    - No persistir direccion ni saldo
       */
      val sujetoJsonParcial =
        s"""
    {
      "EV_ID": $deliveryIdAct,
      "SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "SUJ_DENOMINACION": "Sujeto-Test1",
      "SUJ_EMAIL": "test1@ejemplo.com",
      "SUJ_DIRECCION": "null",
      "SUJ_CAT_SUJ_ID": "999"
    }
    """

      val sujetoModificadoJson =
        s"""
        {
          "EV_ID": $deliveryIdAct,
          "SUJ_IDENTIFICADOR": "${testData.sujetoId}",
          "SUJ_DIRECCION": "Calle falsa 123",
          "SUJ_EMAIL": "null",
          "SUJ_CANAL_ORIGEN": "OTAX"
        }
        """

      for {
        // 1 - Crear VSOA
        _ <- messageProducer.produceEvento(sujetoJsonParcial, "DGR-COP-SUJETO-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        verifyCassandra(testData)
      }

      for {
        // 1 - Crear VSOA
        _ <- messageProducer.produceEvento(sujetoModificadoJson, "DGR-COP-SUJETO-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        verifyCassandra2(testData)
      }

  }
  "Test 2 Alta/Modificacion: de objeto Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      //JSON -> LocalDateTime
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
      val fecha = LocalDateTime.now().format(formatter)

      // Assert -> Data (cassandra) transformar en String
      val formatterCassandra = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val fechaCassandra = LocalDate.now().format(formatterCassandra)

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = fechaCassandra
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s"AND SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s"AND SOJ_TIPO_OBJETO = '${testData.objetoTipo}';"
          )
          .futureValue

        val objeto2 = resultado.one()

        // Persistir la descripcion y fecha_inicio
        objeto2.getString("SOJ_DESCRIPCION") should be("ObjetoPrueba_T2")
        objeto2.getLocalDate("SOJ_FECHA_INICIO").format(formatterCassandra) should be(expectedDate)

        objeto2.getString("SOJ_SUJ_IDENTIFICADOR") should be(testData.sujetoId)

        // State parcial
        objeto2.isNull("SOJ_SUBTIPO") should be(true)
        objeto2.isNull("SOJ_FECHA_ADQ_SUBASTA") should be(true)

        // Persistir el origen, base_imponible
        objeto2.getString("SOJ_CANAL_ORIGEN") should be("OTAX")
        objeto2.getFloat("SOJ_BASE_IMPONIBLE") should be(12345)

        val sojOtrosAtributos = objeto2.getMap("SOJ_OTROS_ATRIBUTOS", classOf[String], classOf[String])
        val sojDetalles: String = sojOtrosAtributos.get("SOJ_DETALLES")

        decode[List[DetallesObjeto]](sojDetalles) match {
          case Left(error) => fail(s"Error decoding SOJ_OTROS_ATRIBUTOS: $error")
          case Right(detalles) =>
            // Persistir soj_detalles: semaforo_marca
            detalles.head.SOJ_SEMAFORO_MARCA.get should be("P")
        }
      }

      def verifyCassandra2(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = fechaCassandra
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s"AND SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s"AND SOJ_TIPO_OBJETO = '${testData.objetoTipo}';"
          )
          .futureValue

        val objeto2 = resultado.one()

        // Mantener la descripcion y fecha_inicio
        objeto2.getString("SOJ_DESCRIPCION") should be("ObjetoPrueba_T2")
        objeto2.getLocalDate("SOJ_FECHA_INICIO").format(formatterCassandra) should be(expectedDate)

        // Persistir nuevo sutbipo y fecha_adq_subasta
        objeto2.getString("SOJ_SUJ_IDENTIFICADOR") should be(testData.sujetoId)
        objeto2.getString("SOJ_SUBTIPO") should be("Urbano")
        objeto2.getLocalDate("SOJ_FECHA_ADQ_SUBASTA").format(formatterCassandra) should be(expectedDate)

        // Eliminar el origen, base_imponible
        objeto2.isNull("SOJ_CANAL_ORIGEN") should be(true)
        objeto2.getFloat("SOJ_BASE_IMPONIBLE") should be(0.0)

        val sojOtrosAtributos = objeto2.getMap("SOJ_OTROS_ATRIBUTOS", classOf[String], classOf[String])
        val sojDetalles: String = sojOtrosAtributos.get("SOJ_DETALLES")

        decode[List[DetallesObjeto]](sojDetalles) match {
          case Left(error) => fail(s"Error decoding SOJ_OTROS_ATRIBUTOS: $error")
          case Right(detalles) =>
            // Persistir soj_detalles: responsable y porcentaje.
            detalles.head.RESPONSABLE_OTROS_ATRIBUTOS.get should be("S")
            detalles.head.PORCENTAJE_OTROS_ATRIBUTOS.get should be(100)

            // No persistir semaforo_color
            detalles.head.SOJ_SEMAFORO_COLOR should be(None)

            // Mantener soj_detalles: semaforo_marca
            detalles.head.SOJ_SEMAFORO_MARCA.get should be("P")
        }
      }

      val testData = TestData(
        sujetoId = "CuitTri_T2",
        objetoId = "ObjetoTri_T2",
        objetoTipo = "A",
        obnId = "Obn_T2"
      )

      val objetoJsonInicial =
        s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "SOJ_DESCRIPCION": "ObjetoPrueba_T2",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_ADQ_SUBASTA": "1000-01-01 00:00:00.0",
      "SOJ_FECHA_INICIO": "$fecha",
      "SOJ_BASE_IMPONIBLE": "12345",
      "SOJ_SUBTIPO": "null",
      "SOJ_CANAL_ORIGEN": "OTAX",
      "SOJ_OTROS_ATRIBUTOS": {
      "SOJ_DETALLES":
      [{"SOJ_SEMAFORO_MARCA": "P"}]}
      }
    """

      val objetoModificadoJson =
        s"""
            {
            "EV_ID": "$deliveryIdAct",
            "SOJ_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
            "SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
            "SOJ_IDENTIFICADOR": "${testData.objetoId}",
            "SOJ_ESTADO": null,
            "SOJ_FECHA_ADQ_SUBASTA": "$fecha",
            "SOJ_BASE_IMPONIBLE": "999",
            "SOJ_SUBTIPO": "Urbano",
            "SOJ_CANAL_ORIGEN": "null",
            "SOJ_OTROS_ATRIBUTOS": {
            "SOJ_DETALLES": [
            {
            "RESPONSABLE_OTROS_ATRIBUTOS": "S",
            "PORCENTAJE_OTROS_ATRIBUTOS": "100",
            "SOJ_SEMAFORO_COLOR": "null"
            }]}
          }
          """

      for {
        // 1 - Alta OBJETO
        _ <- messageProducer.produceEvento(objetoJsonInicial, "DGR-COP-OBJETOS-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        verifyCassandra(testData)
      }

      for {
        // 2 - Modificar Objeto
        _ <- messageProducer.produceEvento(objetoModificadoJson, "DGR-COP-OBJETOS-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyCassandra2(testData)
      }
  }
  "Test 3 Alta/Modificacion: de obligacion Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()

        // Persistir la BOB_SALDO, BOB_ESTADO, BOB_FISCALIZADA y BOB_VENCIMIENTO
        obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(testData.objetoId)
        obligacion.getString("BOB_ESTADO") should be("JUDICIAL")
      }

      val testData = TestData(
        sujetoId = "CuitTri_T3",
        objetoId = "ObjetoTri_T3",
        objetoTipo = "A",
        obnId = "Obn_T3"
      )

      val obligacionJson =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "JUDICIAL",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2024",
      "BOB_FISCALIZADA": "Fiscalizada",
      "BOB_CUOTA": "6",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_INTERES_PUNIT": "1000",
      "BOB_INDICE_INT_PUNIT": "null",
      "BOB_SALDO": "200",
      "BOB_TIPO": "tributaria",
      "BOB_OGA_ID": null,
      "BOB_PLN_ID": "5555",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [
      {
      "PLAN_MULTIOBJETO": "PlanMultiobjeto",
      "RULE_NUMBER": "1"
      }]}
    }"""

      for {
        // 1 - Alta obn
        _ <- messageProducer.produceEvento(obligacionJson, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        verifyCassandra(testData)
      }

  }
  "Test 4 Alta/Modificacion: de obligacion con state parcial" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

      val fecha = LocalDateTime.now().minusDays(5).format(formatter)
      val fecha2 = LocalDateTime.now().minusMonths(3).format(formatter)

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = LocalDateTime.parse("2024-12-21T00:00")
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()

        // Persistir la BOB_SALDO, BOB_ESTADO, BOB_FISCALIZADA y BOB_VENCIMIENTO
        obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(testData.objetoId)
        obligacion.getString("BOB_ESTADO") should be("JUDICIAL")
        obligacion
          .getLocalDate("BOB_VENCIMIENTO")
          .atStartOfDay() should be(expectedDate) //TODO SI EL TEST FALLA ES PORQUE HAY QUE ACTUALIZAR LA FECHA <-
        obligacion.getFloat("BOB_SALDO").toInt should be(200)
        obligacion.getString("BOB_FISCALIZADA") should be("Fiscalizada")

        // No persistir BOB_PRORROGA ni BOB_INDICE_INT_PUNIT
        obligacion.isNull("BOB_PRORROGA") should be(true)
        obligacion.isNull("BOB_INDICE_INT_PUNIT") should be(true)

        val bobOtrosAtributos = obligacion.getMap("BOB_OTROS_ATRIBUTOS", classOf[String], classOf[String])
        val bobDetalles: String = bobOtrosAtributos.get("BOB_DETALLES")

        decode[List[DetallesObligacion]](bobDetalles) match {
          case Left(error) => fail(s"Error decoding BOB_OTROS_ATRIBUTOS: $error")
          case Right(detalles) =>
            // Persistir en bob_detalle el PLAN_MULTIOBJETO
            detalles.head.PLAN_MULTIOBJETO.get should be("PlanMultiobjeto")
        }
      }

      def verifyCassandra2(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = LocalDateTime.parse("2024-09-26T00:00")
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()

        // Mantener la bob_estado y bob_saldo
        obligacion.getString("BOB_ESTADO") should be("JUDICIAL")
        obligacion.getFloat("BOB_SALDO").toInt should be(200)

        // Persistir nuevo BOB_FISCALIZADA y BOB_PRORROGA
        obligacion.getString("BOB_FISCALIZADA") should be("NO Fiscalizada")
        obligacion
          .getLocalDate("BOB_PRORROGA")
          .atStartOfDay() should be(expectedDate) //TODO SI EL TEST FALLA ES PORQUE HAY QUE ACTUALIZAR LA FECHA <-

        //Eliminar el bob_interes_punit, bob_pln_id
        obligacion.isNull("BOB_INTERES_PUNIT") should be(true)
        obligacion.isNull("BOB_PLN_ID") should be(true)

        val bobOtrosAtributos = obligacion.getMap("BOB_OTROS_ATRIBUTOS", classOf[String], classOf[String])
        val bobDetalles: String = bobOtrosAtributos.get("BOB_DETALLES")

        decode[List[DetallesObligacion]](bobDetalles) match {
          case Left(error) => fail(s"Error decoding BOB_OTROS_ATRIBUTOS: $error")
          case Right(detalles) =>
            // Persistir bob_detalles: BOB_MUNICIPIO y JUICIO_MULTIOBJETO.
            detalles.head.BOB_MUNICIPIO.get should be("Municipio")
            detalles.head.JUICIO_MULTIOBJETO.get should be("Multiobjeto")

            // Mantener bob_detalles: PLAN_MULTIOBJETO
            detalles.head.PLAN_MULTIOBJETO.get should be("PlanMultiobjeto")
        }
      }

      val testData = TestData(
        sujetoId = "CuitTri_T4",
        objetoId = "ObjetoTri_T4",
        objetoTipo = "A",
        obnId = "Obn_T4"
      )

      val obligacionJsonInicial =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "JUDICIAL",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "$fecha",
      "BOB_PERIODO": "2024",
      "BOB_FISCALIZADA": "Fiscalizada",
      "BOB_CUOTA": "6",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_INTERES_PUNIT": "1000",
      "BOB_INDICE_INT_PUNIT": "null",
      "BOB_SALDO": "200",
      "BOB_TIPO": "tributaria",
      "BOB_OGA_ID": null,
      "BOB_PLN_ID": "5555",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [
      {
      "PLAN_MULTIOBJETO": "PlanMultiobjeto",
      "RULE_NUMBER": "1"
      }]}
    }"""

      val obligacionModificadoJson =
        s"""{
          "EV_ID": "$deliveryIdAct",
          "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
          "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
          "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
          "BOB_OBN_ID": "${testData.obnId}",
          "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
          "BOB_PRORROGA": "$fecha2",
          "BOB_FISCALIZADA": "NO Fiscalizada",
          "BOB_PERIODO": "2024",
          "BOB_INTERES_PUNIT": "999",
          "BOB_SALDO": "200",
          "BOB_PLN_ID": "null",
          "BOB_OTROS_ATRIBUTOS": {
          "BOB_DETALLES": [
          {
          "BOB_MUNICIPIO": "Municipio",
          "RULE_NUMBER": "1",
          "JUICIO_MULTIOBJETO": "Multiobjeto"
           }]}
        }"""

      //todo PRIMER TEST
      for {
        // 1 - Alta obn
        _ <- messageProducer.produceEvento(obligacionJsonInicial, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyCassandra(testData)
      }

      for {
        // - Pago obn en VSOA
        _ <- messageProducer.produceEvento(obligacionModificadoJson, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyCassandra2(testData)
      }
  }
  "Test 5 Sincro: de cambios masivos sobre misma obn" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher: ExecutionContextExecutor = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val cassandra = context.cassandra

      val sujeto1 = "CuitTri-1-T4"

      val IdObjeto = "ObjetoTri-T4"
      val tipoObjeto = "A"
      val obnId = "1234-T4"

      val n = 10
      for (i <- 1 until n) {

        val obligacionJsonCuit1 =
          s"""{
      "EV_ID": "${i}",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto1",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obnId",
      "BOB_ESTADO": "JUDICIAL",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2024",
      "BOB_FISCALIZADA": "Fiscalizada",
      "BOB_CUOTA": "1",
      "BOB_CAPITAL": "50${i}",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_INTERES_PUNIT": "1000",
      "BOB_INDICE_INT_PUNIT": "null",
      "BOB_SALDO": "201",
      "BOB_TIPO": "tributaria",
      "BOB_OGA_ID": null,
      "BOB_PLN_ID": "5555",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [
      {
      "PLAN_MULTIOBJETO": "PlanMultiobjeto",
      "RULE_NUMBER": "1"
      }]}
    }"""

        messageProducer.produceEvento(obligacionJsonCuit1, "DGR-COP-OBLIGACIONES-TRI")
      }

      eventually(timeout(15.seconds), interval(1.milliseconds)) {
        Thread.sleep(5000)
        // 1 - Queda el registro persistido
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${IdObjeto}' AND BOB_SOJ_TIPO_OBJETO = '${tipoObjeto}' AND BOB_SUJ_IDENTIFICADOR = '${sujeto1}' AND BOB_OBN_ID = '${obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()
        obligacion.getString("BOB_OBN_ID") should be(obnId)
      }
  }
  "Test 6 Sincro: de las obligaciones retroactivas " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

      val testDataA = TestData(
        sujetoId = "CuitTri_T6",
        objetoId = "ObjetoTri_T6",
        objetoTipo = "A",
        obnId = "Obn_T6"
      )
      val testDataB = TestData(
        sujetoId = "CuitTriB_T6",
        objetoId = "ObjetoTri_T6",
        objetoTipo = "A",
        obnId = "Obn_T6"
      )

      def verifyFalseCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s"AND SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s"AND SOJ_TIPO_OBJETO = '${testData.objetoTipo}';"
          )
          .futureValue

        val objetoResult = resultado.one()
        objetoResult.getBoolean("soj_tiene30objeto") should be(false)
        objetoResult.getBoolean("soj_tiene30objetovinculo") should be(false)
      }

      def verifyVinculoCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s"AND SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s"AND SOJ_TIPO_OBJETO = '${testData.objetoTipo}';"
          )
          .futureValue

        val objetoResult = resultado.one()
        objetoResult.getBoolean("soj_tiene30objeto") should be(true)
        objetoResult.getBoolean("soj_tiene30objetovinculo") should be(false)
      }

      def verifyRemovedCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(0)
      }

      val fecha = LocalDateTime.now().minusDays(5)
      val fechaformatter = LocalDateTime.now().minusDays(5).format(formatter)

      val periodo = fecha.getYear
      val cuota = fecha.getMonthValue

      val ObjetoA =
        s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "${testDataA.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testDataA.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testDataA.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val ObjetoB =
        s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "${testDataB.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testDataB.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testDataB.objetoId}",
      "SOJ_DESCRIPCION": "SegundoObjetoPrueba_T2",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val obligacionVencidaSujetoA =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testDataA.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testDataA.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testDataA.objetoId}",
      "BOB_OBN_ID": "${testDataA.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      val obligacionVencidaSujetoB =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testDataB.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testDataB.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testDataB.objetoId}",
      "BOB_OBN_ID": "${testDataB.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      val obligacionPagaSujetoA =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testDataA.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testDataA.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testDataA.objetoId}",
      "BOB_OBN_ID": "${testDataA.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$fechaformatter",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio",
      "RULE_NUMBER": "-1"
      }]}
    }"""

      //todo PRIMER EVENTO

      for {
        // 1 - Crear OBJETOA
        _ <- messageProducer.produceEvento(ObjetoA, "DGR-COP-OBJETOS-TRI")
        // 2 - Crear OBJETOB
        _ <- messageProducer.produceEvento(ObjetoB, "DGR-COP-OBJETOS-TRI")
        // 3 - Alta obn vencida SujetoA
        _ <- messageProducer.produceEvento(obligacionVencidaSujetoA, "DGR-COP-OBLIGACIONES-TRI")
        // 4 - Alta obn vencida SujetoB
        _ <- messageProducer.produceEvento(obligacionVencidaSujetoB, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      Thread.sleep(5000)
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyFalseCassandra(testDataA)
        verifyFalseCassandra(testDataB)
      }

      for {
        // 1 - obn Paga SujetoA
        _ <- messageProducer.produceEvento(obligacionPagaSujetoA, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      Thread.sleep(5000)
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyRemovedCassandra(testDataA)
        verifyVinculoCassandra(testDataA)
      }
  }
  "Test 7 Sincro: de sincronizacion de cuits" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

      val testData = TestData(
        sujetoId = "CuitTri_T7",
        objetoId = "ObjetoTri_T7",
        objetoTipo = "A",
        obnId = "Obn_T7"
      )

      val fecha = LocalDateTime.now().minusDays(5)
      val vtoVencida = LocalDateTime.now().minusDays(25)
      val vtoVencidaParsed = LocalDateTime.now().minusDays(25).format(formatter)

      val vtoMUC = vtoVencida.plusMonths(1).format(formatter)
      val vtoPlazoGracia = LocalDateTime.now().minusDays(5).format(formatter)

      val periodo = fecha.getYear
      val cuota = fecha.getMonthValue
      val obligacionVencidaNoDeuda =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$vtoPlazoGracia",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_ADHERIDO_DEBITO": "N",
      "BOB_TOTAL": "1000000",
      "BOB_PLN_ID": "null",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      val obligacionVencidaMUC =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$vtoVencidaParsed",
      "BOB_VENCIMIENTO_2": "$vtoMUC",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "840",
      "BOB_IMPUESTO": "2",
      "BOB_TIPO": "tributaria",
      "BOB_ADHERIDO_DEBITO": "N",
      "BOB_TOTAL": "99999",
      "BOB_PLN_ID": "null",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      val obligacionVencida =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$vtoVencidaParsed",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""
      // N - 99999 - 10

      def verifyTrueCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s"AND SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s"AND SOJ_TIPO_OBJETO = '${testData.objetoTipo}';"
          )
          .futureValue

        val objetoResult = resultado.one()
        objetoResult.getBoolean("soj_tiene30objeto") should be(true)
        objetoResult.getBoolean("soj_tiene30objetovinculo") should be(true)
      }

      def verifyFalseCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s"AND SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s"AND SOJ_TIPO_OBJETO = '${testData.objetoTipo}';"
          )
          .futureValue

        val objetoResult = resultado.one()
        objetoResult.getBoolean("soj_tiene30objeto") should be(false)
        objetoResult.getBoolean("soj_tiene30objetovinculo") should be(false)
      }

      //todo PRIMER EVENTO

      for {
        //obn vencida no deuda
        _ <- messageProducer.produceEvento(obligacionVencidaNoDeuda, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyTrueCassandra(testData)
      }

      for {
        // obn vencida MUC
        _ <- messageProducer.produceEvento(obligacionVencidaMUC, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyTrueCassandra(testData)
      }

      for {
        //obn vencida
        _ <- messageProducer.produceEvento(obligacionVencida, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyFalseCassandra(testData)
      }
  }
  "Test 8    30%: MUC, Plazo de gracia y Deuda" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val cassandra = context.cassandra

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

      val sujeto = "SUJETO_30_TEST1"
      val tipoObjeto = "A"
      val IdObjeto = "OBJETO30_PRIMERO"
      val IdObjeto2 = "OBJETO30_SEGUNDO"
      val IdObjeto3 = "OBJETO30_TERCERO"
      val obn_id = "obnGracia-T1"
      val obn_id2 = "obnMUC-T1"
      val obn_id3 = "obnDeuda-T1"

      val vtoVencida = LocalDateTime.now().minusDays(25)
      val vtoVencidaParsed = LocalDateTime.now().minusDays(25).format(formatter)

      val vtoMUC = vtoVencida.plusMonths(1).format(formatter)

      val fecha = LocalDateTime.now().minusDays(5)
      val vtoPlazoGracia = LocalDateTime.now().minusDays(5).format(formatter)

      val periodo = fecha.getYear
      val cuota = fecha.getMonthValue

      /*
     El evento debe:
     Persistir el alta Suj1-Obj1-Tipo2
     Objeto1 - Condonacion por sujeto
     Obn con vencimiento fecha < 10 dias de actual.
     Tambien validamos el objeto1 que se cree como tipo 2 por default
       */

      //todo ALTA DE OBJ2 - TIPO2
      val SegundoObjeto30Porciento =
        s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "$sujeto",
      "SOJ_TIPO_OBJETO": "$tipoObjeto",
      "SOJ_IDENTIFICADOR": "$IdObjeto2",
      "SOJ_TITULARIDAD": "CONDOMINIO",
      "SOJ_DESCRIPCION": "SegundoObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """
      Thread.sleep(5000)
      //todo ALTA DE OBJ3 - TIPO2
      val TerceroObjeto30Porciento =
        s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "$sujeto",
      "SOJ_TIPO_OBJETO": "$tipoObjeto",
      "SOJ_IDENTIFICADOR": "$IdObjeto3",
      "SOJ_TITULARIDAD": "CONDOMINIO",
      "SOJ_DESCRIPCION": "SegundoObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """
      Thread.sleep(5000)

      //todo ALTA DE OBJ1 CON OBN VENCIDA - TIPO2
      //FIXME EL CAMPO BOB_OTROS_ATRIBUTOS -> BOB_DETALLES DEBE ESTAR EN EL JSON SINO ROMPE EL DMN
      val obligacionVencidaNoDeuda =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obn_id",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$vtoPlazoGracia",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""
      Thread.sleep(5000)

      val obligacionVencidaMUC =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obn_id2",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$vtoVencidaParsed",
      "BOB_VENCIMIENTO_2": "$vtoMUC",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "840",
      "BOB_IMPUESTO": "2",
      "BOB_TIPO": "tributaria",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""
      Thread.sleep(5000)

      val obligacionVencida =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obn_id3",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "$vtoVencidaParsed",
      "BOB_PERIODO": "$periodo",
      "BOB_CUOTA": "$cuota",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""
      Thread.sleep(5000)

      val testSegundoObjeto30Porciento: Either[io.circe.Error, ObjetosTri] =
        decode[ObjetosTri](SegundoObjeto30Porciento)
      val testTercerObjeto30Porciento: Either[io.circe.Error, ObjetosTri] = decode[ObjetosTri](TerceroObjeto30Porciento)
      val testObligacion30Porciento: Either[io.circe.Error, ObligacionesTri] =
        decode[ObligacionesTri](obligacionVencidaNoDeuda)
      val testObligacionMUC: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionVencidaMUC)
      val testObligacionVencida: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionVencida)

      testSegundoObjeto30Porciento match {
        case Right(objetoInicial) =>
          messageProducer.produceObjeto(objetoInicial)

          testTercerObjeto30Porciento match {
            case Right(objetoInicial) =>
              messageProducer.produceObjeto(objetoInicial)

              testObligacion30Porciento match {
                case Right(obligacionVencidaNoDeuda) =>
                  messageProducer.produceObligacion(obligacionVencidaNoDeuda)

                  eventually(timeout(15.seconds), interval(100.milliseconds)) {

                    val resultadoObligacion1: AsyncResultSet = cassandra.cassandraWrite
                      .cqlSelect(
                        s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' AND BOB_SUJ_IDENTIFICADOR = '${sujeto}' AND BOB_OBN_ID = '$obn_id';"
                      )
                      .futureValue

                    val obligacion1 = resultadoObligacion1.one()

                    //Validar la evaluacion de la obligacion: resultdmn
                    obligacion1.getString("BOB_RESULTDMN") should be("1")

                    val resultadoObjeto1: AsyncResultSet = cassandra.cassandraWrite
                      .cqlSelect(
                        s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                      )
                      .futureValue

                    val objeto1 = resultadoObjeto1.one()

                    // Validar la aplicabilidad del cupon: soj_aplicarDescuento
                    objeto1.getBoolean("soj_aplicarDescuento") should be(true)

                    testObligacionMUC match {
                      case Right(obligacionVencidaMUC) =>
                        messageProducer.produceObligacion(obligacionVencidaMUC)

                        eventually(timeout(15.seconds), interval(100.milliseconds)) {
                          val resultadoObligacion2: AsyncResultSet = cassandra.cassandraWrite
                            .cqlSelect(
                              s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' AND BOB_SUJ_IDENTIFICADOR = '${sujeto}' AND BOB_OBN_ID = '$obn_id2';"
                            )
                            .futureValue

                          val obligacion2 = resultadoObligacion2.one()

                          //Validar la evaluacion de la obligacion: resultdmn
                          obligacion2.getString("BOB_RESULTDMN") should be("1")

                          val resultadoObjeto2: AsyncResultSet = cassandra.cassandraWrite
                            .cqlSelect(
                              s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto2' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                            )
                            .futureValue

                          val objeto2 = resultadoObjeto2.one()

                          // Validar la aplicabilidad del cupon: soj_aplicarDescuento
                          objeto2.getBoolean("soj_aplicarDescuento") should be(true)

                          testObligacionVencida match {
                            case Right(obnVencida) =>
                              messageProducer.produceObligacion(obnVencida)

                              eventually(timeout(15.seconds), interval(100.milliseconds)) {
                                //Obligacion 3 DEUDA
                                val resultadoObligacion3: AsyncResultSet = cassandra.cassandraWrite
                                  .cqlSelect(
                                    s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' AND BOB_SUJ_IDENTIFICADOR = '${sujeto}' AND BOB_OBN_ID = '$obn_id3';"
                                  )
                                  .futureValue

                                val obligacion3 = resultadoObligacion3.one()

                                //Validar la evaluacion de la obligacion: resultdmn
                                obligacion3.getString("BOB_RESULTDMN") should be("-8")

                                //Objeto 1
                                val resultadoObjeto1: AsyncResultSet = cassandra.cassandraWrite
                                  .cqlSelect(
                                    s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                                  )
                                  .futureValue

                                val objeto1 = resultadoObjeto1.one()

                                // Validar la aplicabilidad del cupon: soj_aplicarDescuento
                                objeto1.getBoolean("soj_aplicarDescuento") should be(false)
                                objeto1.getBoolean("soj_tiene30Objeto") should be(false)
                                //Fixme: verificar pq el objeto_vinculo no persiste en su map el primer VSO
                                // objeto1.getBoolean("soj_tiene30ObjetoVinculo") should be(false)

                                //Objeto 2
                                val resultadoObjeto2: AsyncResultSet = cassandra.cassandraWrite
                                  .cqlSelect(
                                    s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto2' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                                  )
                                  .futureValue

                                val objeto2 = resultadoObjeto2.one()

                                // Validar la aplicabilidad del cupon: soj_aplicarDescuento
                                objeto2.getBoolean("soj_aplicarDescuento") should be(true)
                                objeto2.getBoolean("soj_tiene30Objeto") should be(true)
                                //Fixme: verificar pq el objeto_vinculo no persiste en su map el primer VSO
                                // objeto2.getBoolean("soj_tiene30ObjetoVinculo") should be(false)

                                //Objeto 3
                                val resultadoObjeto3: AsyncResultSet = cassandra.cassandraWrite
                                  .cqlSelect(
                                    s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto3' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                                  )
                                  .futureValue

                                val objeto3 = resultadoObjeto3.one()

                                // Validar la aplicabilidad del cupon: soj_aplicarDescuento
                                objeto3.getBoolean("soj_aplicarDescuento") should be(true)
                                objeto3.getBoolean("soj_tiene30Objeto") should be(true)
                                //Fixme: verificar pq el objeto_vinculo no persiste en su map el primer VSO
                                // objeto3.getBoolean("soj_tiene30ObjetoVinculo") should be(false)

                              }

                            // Implicito se sabe que el tiene30sujeto en obn1 y obn2 debe permanecer en true
                            // pero para obn3 -> false.

                            case Left(error) =>
                              println(s"Error decodificando JSON testObligacion30Porciento: $error")
                          }
                        }
                      case Left(error) =>
                        println(s"Error decodificando JSON testObligacion30Porciento: $error")
                    }
                  }
                case Left(error) =>
                  println(s"Error decodificando JSON testObligacion30Porciento: $error")
              }
            case Left(error) =>
              println(s"Error decodificando JSON testTerceroObjeto30Porciento: $error")
          }
        case Left(error) =>
          println(s"Error decodificando JSON testSegundoObjeto30Porciento: $error")
      }
  }
  "Test 9 Pago de Obn sobre una VSO que existe y es valida" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData1 = TestData(
        sujetoId = "CuitTri_T9",
        objetoId = "ObjetoTri_T9",
        objetoTipo = "A",
        obnId = "Obn_T9"
      )

      val VSO_A =
        s"""
      {
      "EV_ID": "1",
      "SOJ_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val altaObligacion =
        s"""{
      "EV_ID": "2",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      val pagoObligacion =
        s"""{
      "EV_ID": "3",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio",
      "RULE_NUMBER": "-1"
      }]}
    }"""

      def verifyObnRemovedCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT count(*) FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(0)
      }

      def verifyObjetoAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_sujeto_objeto" +
            s" WHERE SOJ_TIPO_OBJETO = '${testData1.objetoTipo}'" +
            s" AND SOJ_IDENTIFICADOR = '${testData1.objetoId}'" +
            s" AND SOJ_SUJ_IDENTIFICADOR = '${testData1.sujetoId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }

      //TODO: cuando necesitemos validar campos llamar a
      // verifyValueCassandra(testData: TestData, value: String|Boolean|Float): Assertion
      // verfyDetailsValueCassandra(testData: TestData, value: Any): Assertion
      // {match tipoValue => hacer get de String|Boolean|Float}

      //    1 - Mandar un pago de obn sobre una VSO que existe y es valida - Checked!
      for {
        // 1 - Crear VSOA
        _ <- messageProducer.produceEvento(VSO_A, "DGR-COP-OBJETOS-TRI")
        // 2 - Alta obn en VSO_A
        _ <- messageProducer.produceEvento(altaObligacion, "DGR-COP-OBLIGACIONES-TRI")
        // 3 - Pago obn en VSOA
        _ <- messageProducer.produceEvento(pagoObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      Thread.sleep(5000)
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyObnRemovedCassandra(testData1)
        verifyObjetoAltaCassandra(testData1)
      }
  }
  "Test 10 Mandar un pago de obn sobre una VSO dada de baja " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData1 = TestData(
        sujetoId = "CuitTri_T10",
        objetoId = "ObjetoTri_T10",
        objetoTipo = "A",
        obnId = "Obn_T10"
      )

      val VSO_A =
        s"""
      {
      "EV_ID": "1",
      "SOJ_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val altaObligacion =
        s"""{
      "EV_ID": "2",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      val VSO_A_BAJA =
        s"""
      {
      "EV_ID": "3",
      "SOJ_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": "BAJA",
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val pagoObligacion =
        s"""{
      "EV_ID": "4",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio",
      "RULE_NUMBER": "-1"
      }]}
    }"""

      def verifyObnRemovedCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT count(*) FROM read_side.buc_obligaciones" +
              s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
              s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
              s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
              s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(0)
      }
      def verifyObjetoRemovedCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_sujeto_objeto" +
              s" WHERE SOJ_TIPO_OBJETO = '${testData1.objetoTipo}'" +
              s" AND SOJ_IDENTIFICADOR = '${testData1.objetoId}'" +
              s" AND SOJ_SUJ_IDENTIFICADOR = '${testData1.sujetoId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(0)
      }

      //TODO: cuando necesitemos validar campos llamar a
      // verifyValueCassandra(testData: TestData, value: String|Boolean|Float): Assertion
      // verfyDetailsValueCassandra(testData: TestData, value: Any): Assertion
      // {match tipoValue => hacer get de String|Boolean|Float}

      //    2 - Mandar un pago de obn sobre una VSO dada de baja -> No crear VSO
      for {
        // 1 - Crear VSOA
        _ <- messageProducer.produceEvento(VSO_A, "DGR-COP-OBJETOS-TRI")
        // 2 - Alta obn en VSO_A
        _ <- messageProducer.produceEvento(altaObligacion, "DGR-COP-OBLIGACIONES-TRI")
        // 3 - Dar baja VSO_A
        _ <- messageProducer.produceEvento(VSO_A_BAJA, "DGR-COP-OBJETOS-TRI")
        // 4 - Pagar la Obn
        //        _ <- messageProducer.produceEvento(pagoObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      Thread.sleep(5000)

      for {
        // 3 - Dar baja VSO_A
        _ <- messageProducer.produceEvento(VSO_A_BAJA, "DGR-COP-OBJETOS-TRI")
        // 4 - Pagar la Obn
        //        _ <- messageProducer.produceEvento(pagoObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      Thread.sleep(5000)
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyObnRemovedCassandra(testData1)
        verifyObjetoRemovedCassandra(testData1)
      }
  }
  "Test 11 Mandar un pago de obn sobre una VSO que no existe " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData1 = TestData(
        sujetoId = "CuitTri_T11",
        objetoId = "ObjetoTri_T11",
        objetoTipo = "A",
        obnId = "Obn_T11"
      )

      val pagoObligacion =
        s"""{
      "EV_ID": "1",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio",
      "RULE_NUMBER": "-1"
      }]}
    }"""

      def verifyObnRemovedCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT count(*) FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(0)
      }
      def verifyObjetoRemovedCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_sujeto_objeto" +
            s" WHERE SOJ_TIPO_OBJETO = '${testData1.objetoTipo}'" +
            s" AND SOJ_IDENTIFICADOR = '${testData1.objetoId}'" +
            s" AND SOJ_SUJ_IDENTIFICADOR = '${testData1.sujetoId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(0)
      }

      //TODO: cuando necesitemos validar campos llamar a
      // verifyValueCassandra(testData: TestData, value: String|Boolean|Float): Assertion
      // verfyDetailsValueCassandra(testData: TestData, value: Any): Assertion
      // {match tipoValue => hacer get de String|Boolean|Float}

      //    3 - Mandar un pago de obn sobre una VSO que no existe -> Crear VSO
      for {
        // 1 - Pagar la Obn
        _ <- messageProducer.produceEvento(pagoObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      Thread.sleep(5000)
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyObnRemovedCassandra(testData1)
        verifyObjetoRemovedCassandra(testData1)
      }
  }
  "Test 12 Mandar obn de deuda de una VSO que no existe " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData1 = TestData(
        sujetoId = "CuitTri_T12",
        objetoId = "ObjetoTri_T12",
        objetoTipo = "A",
        obnId = "Obn_T12"
      )

      val altaObligacion =
        s"""{
      "EV_ID": "1",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      def verifyObnAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT count(*) FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }
      def verifyObjetoAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_sujeto_objeto" +
            s" WHERE SOJ_TIPO_OBJETO = '${testData1.objetoTipo}'" +
            s" AND SOJ_IDENTIFICADOR = '${testData1.objetoId}'" +
            s" AND SOJ_SUJ_IDENTIFICADOR = '${testData1.sujetoId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }

      //TODO: cuando necesitemos validar campos llamar a
      // verifyValueCassandra(testData: TestData, value: String|Boolean|Float): Assertion
      // verfyDetailsValueCassandra(testData: TestData, value: Any): Assertion
      // {match tipoValue => hacer get de String|Boolean|Float}

      //    4 - Mandar obn de deuda de una VSO que no existe
      for {
        // 2 - Alta obn en VSO_A
        _ <- messageProducer.produceEvento(altaObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyObnAltaCassandra(testData1)
        verifyObjetoAltaCassandra(testData1)
      }
  }
  "Test 13 Mandar obn de deuda de una VSO dada de baja " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData1 = TestData(
        sujetoId = "CuitTri_T13",
        objetoId = "ObjetoTri_T13",
        objetoTipo = "A",
        obnId = "Obn_T13"
      )

      val VSO_A =
        s"""
      {
      "EV_ID": "1",
      "SOJ_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val VSO_A_BAJA =
        s"""
      {
      "EV_ID": "2",
      "SOJ_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": "BAJA",
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val altaObligacion =
        s"""{
      "EV_ID": "3",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      def verifyObnAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT count(*) FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }
      def verifyObjetoAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_sujeto_objeto" +
            s" WHERE SOJ_TIPO_OBJETO = '${testData1.objetoTipo}'" +
            s" AND SOJ_IDENTIFICADOR = '${testData1.objetoId}'" +
            s" AND SOJ_SUJ_IDENTIFICADOR = '${testData1.sujetoId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }

      //    5 - Mandar obn de deuda de una VSO dada de baja
      for {
        // 1 - Crear VSO_A
        _ <- messageProducer.produceEvento(VSO_A, "DGR-COP-OBJETOS-TRI")
        // 2 - Dar baja VSO_A
        _ <- messageProducer.produceEvento(VSO_A_BAJA, "DGR-COP-OBJETOS-TRI")
        // 3 - Alta obn en VSO_A
        _ <- messageProducer.produceEvento(altaObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyObnAltaCassandra(testData1)
        verifyObjetoAltaCassandra(testData1)
      }
  }
  "Test 14 Mandar obn de deuda de una VSO que existe y es valida " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData1 = TestData(
        sujetoId = "CuitTri_T14",
        objetoId = "ObjetoTri_T14",
        objetoTipo = "A",
        obnId = "Obn_T14"
      )

      val VSO_A =
        s"""
      {
      "EV_ID": "1",
      "SOJ_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "SOJ_DESCRIPCION": "PrimerObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val altaObligacion =
        s"""{
      "EV_ID": "2",
      "BOB_SUJ_IDENTIFICADOR": "${testData1.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData1.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData1.objetoId}",
      "BOB_OBN_ID": "${testData1.obnId}",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2023",
      "BOB_CUOTA": "11",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_TIPO": "tributaria",
      "BOB_PLN_ID": "10",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [{
      "BOB_MUNICIPIO": "Municipio"
      }]}
    }"""

      def verifyObnAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT count(*) FROM read_side.buc_obligaciones" +
            s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
            s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
            s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
            s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }
      def verifyObjetoAltaCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT COUNT(*) FROM read_side.buc_sujeto_objeto" +
            s" WHERE SOJ_TIPO_OBJETO = '${testData1.objetoTipo}'" +
            s" AND SOJ_IDENTIFICADOR = '${testData1.objetoId}'" +
            s" AND SOJ_SUJ_IDENTIFICADOR = '${testData1.sujetoId}';"
          )
          .futureValue

        val obligacionRemoved = resultado.one()
        obligacionRemoved.getLong("count") should be(1)
      }

      //    6 - Mandar obn de deuda de una VSO que existe y es valida
      for {
        // 1 - Crear VSO_A
        _ <- messageProducer.produceEvento(VSO_A, "DGR-COP-OBJETOS-TRI")
        // 2 - Alta obn en VSO_A
        _ <- messageProducer.produceEvento(altaObligacion, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()

      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyObnAltaCassandra(testData1)
        verifyObjetoAltaCassandra(testData1)
      }
  }
  "Test 15 de idempotencia externa" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones" +
              s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
              s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
              s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
              s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()

        // Persistir la BOB_SALDO, BOB_ESTADO, BOB_FISCALIZADA y BOB_VENCIMIENTO
        obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(testData.objetoId)
        obligacion.getString("BOB_ESTADO") should be("JUDICIAL")
      }

      val testData = TestData(
        sujetoId = "CuitTri_T15",
        objetoId = "ObjetoTri_T15",
        objetoTipo = "A",
        obnId = "Obn_T15"
      )

      val obligacionJson =
        s"""{
      "EV_ID": "20",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "JUDICIAL",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2024",
      "BOB_FISCALIZADA": "Fiscalizada",
      "BOB_CUOTA": "6",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_INTERES_PUNIT": "1000",
      "BOB_INDICE_INT_PUNIT": "null",
      "BOB_SALDO": "200",
      "BOB_TIPO": "tributaria",
      "BOB_OGA_ID": null,
      "BOB_PLN_ID": "5555",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [
      {
      "PLAN_MULTIOBJETO": "PlanMultiobjeto",
      "RULE_NUMBER": "1"
      }]}
    }"""

      val objetoJsonInicial =
        s"""
      {
      "EV_ID": "1",
      "SOJ_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "SOJ_DESCRIPCION": "ObjetoPrueba_T2",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_ADQ_SUBASTA": "1000-01-01 00:00:00.0",
      "SOJ_FECHA_INICIO": "1000-01-01 00:00:00.0",
      "SOJ_BASE_IMPONIBLE": "12345",
      "SOJ_SUBTIPO": "null",
      "SOJ_CANAL_ORIGEN": "OTAX",
      "SOJ_OTROS_ATRIBUTOS": {
      "SOJ_DETALLES":
      [{"SOJ_SEMAFORO_MARCA": "P"}]}
      }
    """

      for {
        // 1 - Alta obn
        _ <- messageProducer.produceEvento(obligacionJson, "DGR-COP-OBLIGACIONES-TRI")
        // 2- Alta objeto
        _ <- messageProducer.produceEvento(objetoJsonInicial, "DGR-COP-OBJETOS-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyCassandra(testData)
      }
  }
  "Test 16 Alta/Modificacion: de obligacion Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones" +
              s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
              s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
              s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
              s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()

        // Persistir la BOB_SALDO, BOB_ESTADO, BOB_FISCALIZADA y BOB_VENCIMIENTO
        obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(testData.objetoId)
        obligacion.getString("BOB_ESTADO") should be("ADMINISTRATIVA")
      }

      val testData = TestData(
        sujetoId = "30-50279317-5",
        objetoId = "301000405",
        objetoTipo = "E",
        obnId = "20240000000070012852"
      )

      val obligacionJson =
        s"""{
        "EV_ID" : "12",
        "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
        "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
        "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
        "BOB_OBN_ID": "${testData.obnId}",
        "BOB_SALDO" : "-220983145.06",
        "BOB_CUOTA" : "10",
        "BOB_ESTADO" : "ADMINISTRATIVA",
        "BOB_SUB_ESTADO" : null,
        "BOB_CANAL_ORIGEN" : "OTAX",
        "BOB_FISCALIZADA" : "N",
        "BOB_INDICE_INT_PUNIT" : null,
        "BOB_INDICE_INT_RESAR" : null,
        "BOB_INTERES_PUNIT" : null,
        "BOB_INTERES_RESAR" : null,
        "BOB_JUI_ID" : null,
        "BOB_PERIODO" : "2024",
        "BOB_PLN_ID" : null,
        "BOB_PRORROGA" : "2024-10-24 00:00:00.0",
        "BOB_TIPO" : "tributaria",
        "BOB_TOTAL" :  "-220983145.06",
        "BOB_VENCIMIENTO" : "2024-10-24 00:00:00.0",
        "BOB_CAPITAL" : "-220983145.06",
        "BOB_CONCEPTO" : "890",
        "BOB_IMPUESTO" : "902",
        "FECHA_BAJA" : null,
        "BOB_ADHERIDO_DEBITO" : "N",
        "BOB_OGA_ID" : "90224890",
        "BOB_VENCIMIENTO_2" : null,
        "SOJ_ID_EXTERNO" : "173110",
        "BOB_OTROS_ATRIBUTOS" : {
          "BOB_DETALLES" : [ {
            "EVO_OBN_PEO_ID_MATERIAL" : "PTET",
            "BOB_MUNICIPIO" : null,
            "BOB_INTERES_FINANCIACION" : null,
            "JUICIO_MULTIOBJETO" : "N",
            "RULE_NUMBER" : "22",
            "EVO_OBN_PEO_ID_FORMAL" : "PC",
            "PLAN_MULTIOBJETO" : "N"
          } ]
        }
      }"""

      for {
        // Añadir println antes de producir el evento
        _ <- {
          println(s"[DEBUG] Valor BOB_SALDO en mensaje Kafka: ${
            import spray.json._
            JsonParser(obligacionJson).asJsObject.fields("BOB_SALDO")
          }")
          messageProducer.produceEvento(obligacionJson, "DGR-COP-OBLIGACIONES-TRI")
        }
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        println("Validando...")
        verifyCassandra(testData)
      }
  }

  "Test 20 Alta: de sujeto Tri con identificador vacío" should "llegar al tópico de error" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      val testData = TestData(
        sujetoId = "",
        objetoId = "ObjetoTri_T1",
        objetoTipo = "A",
        obnId = "Obn_T1"
      )

      val sujetoJsonParcial =
        s"""
        {
          "EV_ID": $deliveryIdAct,
          "SUJ_IDENTIFICADOR": "",
          "SUJ_DENOMINACION": "Sujeto-Test1",
          "SUJ_EMAIL": "test1@ejemplo.com",
          "SUJ_DIRECCION": "null",
          "SUJ_CAT_SUJ_ID": "999"
        }
        """

      for {
        _ <- messageProducer.produceEvento(sujetoJsonParcial, "DGR-COP-SUJETO-TRI")
      } yield ()

      eventually(timeout(15.seconds), interval(100.milliseconds)) {
      }
  }
  "Test 21 Alta/Modificacion: de objeto Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      //JSON -> LocalDateTime
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
      val fecha = LocalDateTime.now().format(formatter)

      // Assert -> Data (cassandra) transformar en String
      val formatterCassandra = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val fechaCassandra = LocalDate.now().format(formatterCassandra)

      def verifyCassandra(objetoJsonInicial: String)(implicit ec: ExecutionContext): Assertion = {
        val expectedDate = fechaCassandra
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_sujeto_objeto " +
              s"WHERE SOJ_SUJ_IDENTIFICADOR = '27-23280107-2'" +
              s"AND SOJ_IDENTIFICADOR = 'EKY031'" +
              s"AND SOJ_TIPO_OBJETO = 'A';"
          )
          .futureValue

        val objeto2 = resultado.one()

        // State parcial
        objeto2.getString("SOJ_IDENTIFICADOR") should be("EKY031")
      }

      val objetoJsonInicial =
           s"""{
	        "EV_ID": "1",
	        "SOJ_SUJ_IDENTIFICADOR": "27-23280107-2",
	        "SOJ_TIPO_OBJETO": "A",
	        "SOJ_IDENTIFICADOR": "EKY031",
	        "SOJ_DESCRIPCION": "PEUGEOT SEDAN 3 PUERTAS 206 XR 1.4 3P",
	        "SOJ_ESTADO": "null",
	        "SOJ_FECHA_ADQ_SUBASTA": "1000-01-01 00:00:00.0",
	        "SOJ_FECHA_VTA_SUBASTA": "1000-01-01 00:00:00.0",
	        "SOJ_FECHA_FIN": "1000-01-01 00:00:00.0",
	        "SOJ_FECHA_INICIO": "2004-04-22 00:00:00.0",
	        "SOJ_SUBTIPO": "null",
	        "SOJ_ADHERIDO_DEBITO": "N",
	        "SOJ_CANAL_ORIGEN": "OTAX",
	        "SOJ_CAT_SOJ_ID": "TRI",
	        "SOJ_TITULARIDAD": "null",
	        "SOJ_ID_EXTERNO": "4304977",
	        "SOJ_OTROS_ATRIBUTOS": {
	        	"SOJ_DETALLES": [
	        		{
				"RESPONSABLE_OTROS_ATRIBUTOS": "S",
				"PORCENTAJE_OTROS_ATRIBUTOS": "100",
				"OTROS_ATRIBUTOS_ADHERIDO_DEBITO": "N",
				"SOJ_SEMAFORO_COLOR": "null",
				"SOJ_SEMAFORO_MARCA": "null"
			}]}
          }"""

      val objetoJsonNoBase =
        s"""{
        "EV_ID": "2",
        "SOJ_SUJ_IDENTIFICADOR": "27-27551656-8",
        "SOJ_TIPO_OBJETO": "I",
        "SOJ_IDENTIFICADOR": "110123140729",
        "SOJ_OTROS_ATRIBUTOS" : {
          "SOJ_DETALLES" : [ {
          "RESPONSABLE_OTROS_ATRIBUTOS" : "S",
          "PORCENTAJE_OTROS_ATRIBUTOS" : "100",
          "SOJ_SEMAFORO_COLOR" : "R",
          "SOJ_SEMAFORO_MARCA" : "I"
        } ]
        }
      }"""

      val objetoJsonModificado =
        s"""{
        "EV_ID": "3",
        "SOJ_SUJ_IDENTIFICADOR": "27-23280107-2",
	    "SOJ_TIPO_OBJETO": "A",
	    "SOJ_IDENTIFICADOR": "EKY031",
        "SOJ_OTROS_ATRIBUTOS" : {
          "SOJ_DETALLES" : [ {
          "RESPONSABLE_OTROS_ATRIBUTOS" : "S",
          "PORCENTAJE_OTROS_ATRIBUTOS" : "100",
          "SOJ_SEMAFORO_COLOR" : "R",
          "SOJ_SEMAFORO_MARCA" : "I"
        } ]
        }
      }"""

      Thread.sleep(5000)
      for {
        // 1 - Alta OBJETO
        _ <- messageProducer.produceEvento(objetoJsonInicial, "DGR-COP-OBJETOS-TRI")
        //2 - Alta objeto inexistente en la base (no deberia persistir)
        _ <- messageProducer.produceEvento(objetoJsonNoBase, "DGR-COP-OBJETOS-TRI")
        //3 - Alta objeto existente en la base (deberia actualizar)
        _ <- messageProducer.produceEvento(objetoJsonModificado, "DGR-COP-OBJETOS-TRI")

      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        Thread.sleep(5000)
        verifyCassandra(objetoJsonInicial)
      }

  }
  "Test 22 Alta/Modificacion: de obligacion Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer

      def verifyCassandra(testData: TestData)(implicit ec: ExecutionContext): Assertion = {
        val cassandra = context.cassandra
        val resultado: AsyncResultSet = cassandra.cassandraWrite
          .cqlSelect(
            s"SELECT * FROM read_side.buc_obligaciones" +
              s" WHERE BOB_SOJ_IDENTIFICADOR = '${testData.objetoId}'" +
              s" AND BOB_SOJ_TIPO_OBJETO = '${testData.objetoTipo}'" +
              s" AND BOB_SUJ_IDENTIFICADOR = '${testData.sujetoId}'" +
              s" AND BOB_OBN_ID = '${testData.obnId}';"
          )
          .futureValue

        val obligacion = resultado.one()

        // Persistir la BOB_SALDO, BOB_ESTADO, BOB_FISCALIZADA y BOB_VENCIMIENTO
        obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(testData.objetoId)
        obligacion.getString("BOB_ESTADO") should be("JUDICIAL")
      }

      val testData = TestData(
        sujetoId = "CuitTri_T3",
        objetoId = "ObjetoTri_T3",
        objetoTipo = "A",
        obnId = "Obn_T3"
      )

      val obligacionJson =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "${testData.sujetoId}",
      "BOB_SOJ_TIPO_OBJETO": "${testData.objetoTipo}",
      "BOB_SOJ_IDENTIFICADOR": "${testData.objetoId}",
      "BOB_OBN_ID": "${testData.obnId}",
      "BOB_ESTADO": "JUDICIAL",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2024",
      "BOB_FISCALIZADA": "Fiscalizada",
      "BOB_CUOTA": "6",
      "BOB_CAPITAL": "200",
      "BOB_CONCEPTO": "601",
      "BOB_IMPUESTO": "600",
      "BOB_INTERES_PUNIT": "1000",
      "BOB_INDICE_INT_PUNIT": "null",
      "BOB_SALDO": "200",
      "BOB_TIPO": "tributaria",
      "BOB_OGA_ID": null,
      "BOB_PLN_ID": "5555",
      "BOB_OTROS_ATRIBUTOS": {
      "BOB_DETALLES": [
      {
      "PLAN_MULTIOBJETO": "PlanMultiobjeto",
      "RULE_NUMBER": "1"
      }]}
    }"""

      for {
        // 1 - Alta obn
        _ <- messageProducer.produceEvento(obligacionJson, "DGR-COP-OBLIGACIONES-TRI")
      } yield ()
      eventually(timeout(15.seconds), interval(100.milliseconds)) {
        verifyCassandra(testData)
      }

  }}
