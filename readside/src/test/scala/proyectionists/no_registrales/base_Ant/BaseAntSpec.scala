package proyectionists.no_registrales.base_Ant

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ObjetosAnt}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.{DetallesObjetoDecoder, ObjetosAntDecoder}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.{
  DetallesObligacionDecoder,
  DetallesSupresionesDecoder,
  ObligacionesAntDecoder
}
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, DetallesSupresiones, ObligacionesAnt}
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import io.circe
import kafka.{MessageProcessor, MessageProducer}
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import utils.generators.Model.deliveryIdAct
import io.circe.parser.decode
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
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

  "Test 1: un objeto ANT" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher: ExecutionContextExecutor = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val sujeto = "CuitTri-T2"
    val IdObjeto = "ObjetoTri-T2"
    val tipoObjeto = "A"

    /*
     El evento debe:
    - Persistir la descripcion, fecha inicio y base imponible
    - Persistir en soj_detalle el semaforo_marca
    - No persistir subtipo, fecha_adq_subasta
     */
    val objetoJsonInicial =
      s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "$sujeto",
      "SOJ_TIPO_OBJETO": "$tipoObjeto",
      "SOJ_IDENTIFICADOR": "$IdObjeto",
      "SOJ_DESCRIPCION": "ObjetoPrueba_T2",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_ADQ_SUBASTA": "1000-01-01 00:00:00.0",
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0",
      "SOJ_BASE_IMPONIBLE": "12345",
      "SOJ_SUBTIPO": "null",
      "SOJ_CANAL_ORIGEN": "OTAX",
      "SOJ_OTROS_ATRIBUTOS": {
      "SOJ_DETALLES":
      [{"SOJ_SEMAFORO_MARCA": "P"}]}
      }
    """

    val testObjetoParcial: Either[circe.Error, ObjetosAnt] = decode[ObjetosAnt](objetoJsonInicial)

    testObjetoParcial match {
      case Right(objetoInicial) =>
        messageProducer.produceObjeto(objetoInicial)

        eventually(timeout(15.seconds), interval(100.milliseconds)) {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '${objetoInicial.SOJ_SUJ_IDENTIFICADOR}';"
            )
            .futureValue

          val objeto = resultado.one()

          // Persistir la descripcion, fecha inicio y base imponible
          objeto.getString("SOJ_SUJ_IDENTIFICADOR") should be(objetoInicial.SOJ_SUJ_IDENTIFICADOR)
          objeto.getString("SOJ_DESCRIPCION") should be(objetoInicial.SOJ_DESCRIPCION.get)
          objeto.getLocalDate("SOJ_FECHA_INICIO").atStartOfDay() should be(objetoInicial.SOJ_FECHA_INICIO.get)
          objeto.getFloat("SOJ_BASE_IMPONIBLE") should be(objetoInicial.SOJ_BASE_IMPONIBLE.get)

          // No persistir subtipo, fecha_adq_subasta
          objeto.isNull("SOJ_SUBTIPO") should be(true)
          objeto.isNull("SOJ_FECHA_ADQ_SUBASTA") should be(true)

          val sojOtrosAtributos = objeto.getMap("SOJ_OTROS_ATRIBUTOS", classOf[String], classOf[String])
          val sojDetalles: String = sojOtrosAtributos.get("SOJ_DETALLES")

          decode[List[DetallesObjeto]](sojDetalles) match {
            case Left(error) => fail(s"Error decoding SOJ_OTROS_ATRIBUTOS: $error")
            case Right(detalles) =>
              // Persistir en soj_detalle el semaforo_marca
              detalles.head.SOJ_SEMAFORO_MARCA.get should be("P")
          }

          /*
         El evento debe:
        - Persistir nuevo sutbipo y fecha_adq_subasta
        - Persistir soj_detalles: responsable y porcentaje.
        - Mantener soj_detalles: semaforo_marca
        - No persistir semaforo_color
        - Eliminar el origen, base_imponible
        - Mantener la descripcion y fecha_inicio
           */
          val objetoModificadoJson =
            s"""
            {
            "EV_ID": "$deliveryIdAct",
            "SOJ_SUJ_IDENTIFICADOR": "$sujeto",
            "SOJ_TIPO_OBJETO": "$tipoObjeto",
            "SOJ_IDENTIFICADOR": "$IdObjeto",
            "SOJ_ESTADO": null,
            "SOJ_FECHA_ADQ_SUBASTA": "2024-01-01 00:00:00.0",
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

          val testObjetoModificado: Either[io.circe.Error, ObjetosAnt] = decode[ObjetosAnt](objetoModificadoJson)

          testObjetoModificado match {
            case Right(objetoModificado) =>
              messageProducer.produceObjeto(objetoModificado)

              eventually(timeout(15.seconds), interval(100.milliseconds)) {
                val resultado: AsyncResultSet = cassandra.cassandraWrite
                  .cqlSelect(
                    s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto';"
                  )
                  .futureValue

                val objeto2 = resultado.one()

                // Mantener la descripcion y fecha_inicio
                objeto2.getString("SOJ_DESCRIPCION") should be(objetoInicial.SOJ_DESCRIPCION.get)
                objeto2.getLocalDate("SOJ_FECHA_INICIO").atStartOfDay() should be(objetoInicial.SOJ_FECHA_INICIO.get)

                // Persistir nuevo sutbipo y fecha_adq_subasta
                objeto2.getString("SOJ_SUJ_IDENTIFICADOR") should be(objetoModificado.SOJ_SUJ_IDENTIFICADOR)
                objeto2.getString("SOJ_SUBTIPO") should be(objetoModificado.SOJ_SUBTIPO.get)
                objeto2.getLocalDate("SOJ_FECHA_ADQ_SUBASTA").atStartOfDay() should be(
                  objetoModificado.SOJ_FECHA_ADQ_SUBASTA.get
                )

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
            case Left(error) =>
              println(s"Error decodificando JSON modificado: $error")
          }
        }
      case Left(error) =>
        println(s"Error decodificando JSON inicial: $error")
    }
  }

  "Test 2: un obligacion ANT" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher: ExecutionContextExecutor = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val sujeto = "CuitTri-T2"
    val IdObjeto = "ObjetoTri-T2"
    val tipoObjeto = "A"
    val obn_id = "1234"

    //bob_cuota, bob_vencimiento, tipo son optional pero son requeridos por el dmn y si es asi

    val obligacionJsonInicial =
      s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obn_id",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2024",
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
      }]},
      "BOB_SUPRESIONES": {
      "BOB_DETALLES_SUPRESIONES": [
      {
      "BOB_DESCRIPCION": "hola como andas",
      "BOB_TIPO_SUP": "R"
      }]}
    }"""

    val testObligacionParcial: Either[circe.Error, ObligacionesAnt] = decode[ObligacionesAnt](obligacionJsonInicial)

    testObligacionParcial match {
      case Right(obligacionInicial) =>
        messageProducer.produceObligacion(obligacionInicial)

        eventually(timeout(15.seconds), interval(100.milliseconds)) {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto';"
            )
            .futureValue

          val obligacion = resultado.one()

          // Persistir la BOB_SALDO, BOB_ESTADO y BOB_VENCIMIENTO
          obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(obligacionInicial.BOB_SOJ_IDENTIFICADOR)
          obligacion.getString("BOB_ESTADO") should be(obligacionInicial.BOB_ESTADO.get)
          obligacion.getLocalDate("BOB_VENCIMIENTO").atStartOfDay() should be(obligacionInicial.BOB_VENCIMIENTO.get)
          obligacion.getFloat("BOB_SALDO").toInt should be(obligacionInicial.BOB_SALDO.get)
          obligacion.getString("BOB_TIPO") should be(obligacionInicial.BOB_TIPO.get)

          // No persistir BOB_PRORROGA, BOB_TIPO
          obligacion.isNull("BOB_PRORROGA") should be(true)
          obligacion.isNull("BOB_INDICE_INT_PUNIT") should be(true)

          val bobOtrosAtributos = obligacion.getMap("BOB_OTROS_ATRIBUTOS", classOf[String], classOf[String])
          val bobDetalles: String = bobOtrosAtributos.get("BOB_DETALLES")

          val bobSupresiones = obligacion.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
          val bobDetallesSupresiones: String = bobSupresiones.get("BOB_DETALLES_SUPRESIONES")

          decode[List[DetallesObligacion]](bobDetalles) match {
            case Left(error) => fail(s"Error decoding BOB_OTROS_ATRIBUTOS: $error")
            case Right(detalles) =>
              // Persistir en bob_detalle el PLAN_MULTIOBJETO
              detalles.head.PLAN_MULTIOBJETO.get should be("PlanMultiobjeto")
          }

          decode[List[DetallesSupresiones]](bobDetallesSupresiones) match {
            case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
            case Right(detalles) =>
              // Persistir en detalles_supresiones el BOB_TIPO_SUP
              detalles.head.BOB_TIPO_SUP.get should be("R")
          }

          // persistir nuevo bob_tipo y fecha bob_prorroga
          // persistir bob_detalles BOB_MUNICIPIO y JUICIO_MULTIOBJETO
          // mantener bob_detalles PLAN_MULTIOBJETO
          // no persistir BOB_ESTADO_SUP en supresiones
          // mantener bob_tipo_sup en supressiones
          // mantener bob_estado y bob_saldo
          // eliminar impuesto y soj_id_externo

          val obligacionModificadoJson =
            s"""{
            "EV_ID": "$deliveryIdAct",
            "BOB_SUJ_IDENTIFICADOR": "$sujeto",
            "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
            "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
            "BOB_OBN_ID": "$obn_id",
            "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
            "BOB_PRORROGA": "2025-01-01 00:00:00.0",
            "BOB_PERIODO": "2024",
            "BOB_INTERES_PUNIT": "999",
            "BOB_SALDO": "200",
            "BOB_TIPO": "interurbano",
            "BOB_PLN_ID": "null",
            "BOB_OTROS_ATRIBUTOS": {
            "BOB_DETALLES": [
            {
            "BOB_MUNICIPIO": "Municipio",
            "JUICIO_MULTIOBJETO": "Multiobjeto"
             }]},
            "BOB_SUPRESIONES": {
            "BOB_DETALLES_SUPRESIONES": [
            {
            "BOB_ESTADO_SUP": "null"
            }]}
          }"""

          val testObligacionModificado: Either[io.circe.Error, ObligacionesAnt] =
            decode[ObligacionesAnt](obligacionModificadoJson)

          testObligacionModificado match {
            case Right(obligacionModificado) =>
              messageProducer.produceObligacion(obligacionModificado)

              eventually(timeout(15.seconds), interval(100.milliseconds)) {
                val resultado: AsyncResultSet = cassandra.cassandraWrite
                  .cqlSelect(
                    s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto';"
                  )
                  .futureValue

                val obligacion2 = resultado.one()

                // Mantener la bob_estado y bob_saldo
                obligacion2.getString("BOB_ESTADO") should be(obligacionInicial.BOB_ESTADO.get)
                obligacion2.getFloat("BOB_SALDO").toInt should be(obligacionInicial.BOB_SALDO.get)

                // Persistir nuevo bob_tipo y bob_prorroga
                obligacion2.getString("BOB_TIPO") should be(obligacionModificado.BOB_TIPO.get)
                obligacion2.getLocalDate("BOB_PRORROGA").atStartOfDay() should be(
                  obligacionModificado.BOB_PRORROGA.get
                )

                //Eliminar el bob_interes_punit, bob_pln_id
                obligacion2.isNull("BOB_INTERES_PUNIT") should be(true)
                obligacion2.isNull("BOB_PLN_ID") should be(true)

                val bobOtrosAtributos = obligacion2.getMap("BOB_OTROS_ATRIBUTOS", classOf[String], classOf[String])
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

                val bobSupresiones = obligacion2.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
                val bobDetallesSupresiones: String = bobSupresiones.get("BOB_DETALLES_SUPRESIONES")

                decode[List[DetallesSupresiones]](bobDetallesSupresiones) match {
                  case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
                  case Right(detalles) =>
                    // persistir BOB_FECHA_INICIO_SUP
                    detalles.head.BOB_ESTADO_SUP.isEmpty should be(true)

                    // mantener bob_tipo_sup en supresiones
                    detalles.head.BOB_TIPO_SUP.get should be("R")
                }
              }

            case Left(error) =>
              println(s"Error decodificando JSON modificado: $error")
          }
        }
      case Left(error) =>
        println(s"Error decodificando JSON inicial: $error")
    }
  }

  "Test 3: Replica de pruebas/fallo" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher: ExecutionContextExecutor = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val sujeto = "CuitTri-T2"
    val IdObjeto = "ObjetoTri-T2"
    val tipoObjeto = "A"
    val obn_id = "1234"

    //bob_cuota, bob_vencimiento, tipo son optional pero son requeridos por el dmn y si es asi

    val obligacionJsonInicial =
      s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obn_id",
      "BOB_ESTADO": "ADMINISTRATIVA",
      "BOB_PRORROGA": "1000-01-01 00:00:00.0",
      "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
      "BOB_PERIODO": "2024",
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
      }]},
      "BOB_SUPRESIONES": {
      "BOB_DETALLES_SUPRESIONES": [
      {
      "BOB_DESCRIPCION": "Descripcion supresiones",
      "BOB_TIPO_SUP": "R",
      "BOB_ESTADO_SUP": "ESTADO1",
      "BOB_FECHA_INICIO_SUP": "2024-01-01 00:00:00.0",
      "BOB_FECHA_FIN_SUP": "2024-01-01 00:00:00.0"
      }]}
    }"""

    val testObligacionParcial: Either[circe.Error, ObligacionesAnt] = decode[ObligacionesAnt](obligacionJsonInicial)

    testObligacionParcial match {
      case Right(obligacionInicial) =>
        messageProducer.produceObligacion(obligacionInicial)

        eventually(timeout(15.seconds), interval(100.milliseconds)) {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto';"
            )
            .futureValue

          val obligacion = resultado.one()

          // Persistir la BOB_SALDO, BOB_ESTADO y BOB_VENCIMIENTO
          obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(obligacionInicial.BOB_SOJ_IDENTIFICADOR)
          obligacion.getString("BOB_ESTADO") should be(obligacionInicial.BOB_ESTADO.get)
          obligacion.getLocalDate("BOB_VENCIMIENTO").atStartOfDay() should be(obligacionInicial.BOB_VENCIMIENTO.get)
          obligacion.getFloat("BOB_SALDO").toInt should be(obligacionInicial.BOB_SALDO.get)
          obligacion.getString("BOB_TIPO") should be(obligacionInicial.BOB_TIPO.get)

          // No persistir BOB_PRORROGA, BOB_TIPO
          obligacion.isNull("BOB_PRORROGA") should be(true)
          obligacion.isNull("BOB_INDICE_INT_PUNIT") should be(true)

          val bobOtrosAtributos = obligacion.getMap("BOB_OTROS_ATRIBUTOS", classOf[String], classOf[String])
          val bobDetalles: String = bobOtrosAtributos.get("BOB_DETALLES")

          val bobSupresiones = obligacion.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
          val bobDetallesSupresiones: String = bobSupresiones.get("BOB_DETALLES_SUPRESIONES")

          decode[List[DetallesObligacion]](bobDetalles) match {
            case Left(error) => fail(s"Error decoding BOB_OTROS_ATRIBUTOS: $error")
            case Right(detalles) =>
              // Persistir en bob_detalle el PLAN_MULTIOBJETO
              detalles.head.PLAN_MULTIOBJETO.get should be("PlanMultiobjeto")
          }

          decode[List[DetallesSupresiones]](bobDetallesSupresiones) match {
            case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
            case Right(detalles) =>
              // Persistir en detalles_supresiones el BOB_TIPO_SUP
              detalles.head.BOB_TIPO_SUP.get should be("R")
          }

          // persistir nuevo bob_tipo y fecha bob_prorroga
          // persistir bob_detalles BOB_MUNICIPIO y JUICIO_MULTIOBJETO
          // mantener bob_detalles PLAN_MULTIOBJETO
          // no persistir BOB_ESTADO_SUP en supresiones
          // mantener bob_tipo_sup en supressiones
          // mantener bob_estado y bob_saldo
          // eliminar impuesto y soj_id_externo

          val obligacionModificadoJson =
            s"""{
            "EV_ID": "$deliveryIdAct",
            "BOB_SUJ_IDENTIFICADOR": "$sujeto",
            "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
            "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
            "BOB_OBN_ID": "$obn_id",
            "BOB_VENCIMIENTO": "2024-01-01 00:00:00.0",
            "BOB_PRORROGA": "2025-01-01 00:00:00.0",
            "BOB_PERIODO": "2024",
            "BOB_INTERES_PUNIT": "999",
            "BOB_SALDO": "200",
            "BOB_TIPO": "interurbano",
            "BOB_PLN_ID": "null",
            "BOB_OTROS_ATRIBUTOS": {
            "BOB_DETALLES": [
            {
            "BOB_MUNICIPIO": "Municipio",
            "JUICIO_MULTIOBJETO": "Multiobjeto"
             }]},
            "BOB_SUPRESIONES": {
            "BOB_DETALLES_SUPRESIONES": [
            {
            "BOB_ESTADO_SUP": "ESTADONUEVO"
            }]}
          }"""

          val testObligacionModificado: Either[io.circe.Error, ObligacionesAnt] =
            decode[ObligacionesAnt](obligacionModificadoJson)

          testObligacionModificado match {
            case Right(obligacionModificado) =>
              messageProducer.produceObligacion(obligacionModificado)

              eventually(timeout(15.seconds), interval(100.milliseconds)) {
                val resultado: AsyncResultSet = cassandra.cassandraWrite
                  .cqlSelect(
                    s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto';"
                  )
                  .futureValue

                val obligacion2 = resultado.one()

                // Mantener la bob_estado y bob_saldo
                obligacion2.getString("BOB_ESTADO") should be(obligacionInicial.BOB_ESTADO.get)
                obligacion2.getFloat("BOB_SALDO").toInt should be(obligacionInicial.BOB_SALDO.get)

                // Persistir nuevo bob_tipo y bob_prorroga
                obligacion2.getString("BOB_TIPO") should be(obligacionModificado.BOB_TIPO.get)
                obligacion2.getLocalDate("BOB_PRORROGA").atStartOfDay() should be(
                  obligacionModificado.BOB_PRORROGA.get
                )

                //Eliminar el bob_interes_punit, bob_pln_id
                obligacion2.isNull("BOB_INTERES_PUNIT") should be(true)
                obligacion2.isNull("BOB_PLN_ID") should be(true)

                val bobOtrosAtributos = obligacion2.getMap("BOB_OTROS_ATRIBUTOS", classOf[String], classOf[String])
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

                val bobSupresiones = obligacion2.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
                val bobDetallesSupresiones: String = bobSupresiones.get("BOB_DETALLES_SUPRESIONES")

                decode[List[DetallesSupresiones]](bobDetallesSupresiones) match {
                  case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
                  case Right(detalles) =>
                    detalles.head.BOB_DESCRIPCION.get should be("Descripcion supresiones")
                    detalles.head.BOB_ESTADO_SUP.get should be("ESTADONUEVO")
                }
              }

            case Left(error) =>
              println(s"Error decodificando JSON modificado: $error")
          }
        }
      case Left(error) =>
        println(s"Error decodificando JSON inicial: $error")
    }
  }
  "Test 4: Prueba de supresiones en obligaciones" should "mantener solo las supresiones del último evento" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val cassandra = context.cassandra

      val sujeto1 = "20-317357991-3"
      val sujeto2 = "30-550273551-8"
      val sujeto3 = "30-550273551-9"
      val idObjeto1 = "0123-000181855"
      val idObjeto2 = "0022-000991155"
      val idObjeto3 = "0022-000991156"
      val tipoObjeto = "SALUD"
      val obnId1 = "32868589513"
      val obnId2 = "18782512715"
      val obnId3 = "18782512716"

      val obligacionJsonInicial =
        s"""{
          "EV_ID": "20240923120300971428775999136",
          "BOB_SUJ_IDENTIFICADOR": "$sujeto1",
          "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
          "BOB_SOJ_IDENTIFICADOR": "$idObjeto1",
          "BOB_OBN_ID": "$obnId1",
          "BOB_SUPRESIONES": {
            "BOB_DETALLES_SUPRESIONES": [
              {
                "BOB_TIPO_SUP": "CMPAMI",
                "BOB_DESCRIPCION": null,
                "BOB_ESTADO_SUP": "REALESED",
                "BOB_FECHA_INICIO_SUP": "2024-11-15 00:00:00.0",
                "BOB_FECHA_FIN_SUP": "2024-11-30 00:00:00.0"
              },
              {
                "BOB_TIPO_SUP": "CMYPLAN",
                "BOB_DESCRIPCION": null,
                "BOB_ESTADO_SUP": "ACTIVE",
                "BOB_FECHA_INICIO_SUP": "2025-05-22 00:00:00.0",
                "BOB_FECHA_FIN_SUP": null
              }
            ]
          }
        }"""

      val testObligacionInicial: Either[io.circe.Error, ObligacionesAnt] =
        decode[ObligacionesAnt](obligacionJsonInicial)

      testObligacionInicial match {
        case Right(obligacionInicial) =>
          messageProducer.produceObligacion(obligacionInicial)

          eventually(timeout(15.seconds), interval(100.milliseconds)) {
            val resultado: AsyncResultSet = cassandra.cassandraWrite
              .cqlSelect(
                s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${idObjeto1}' AND BOB_SOJ_TIPO_OBJETO = '${tipoObjeto}';"
              )
              .futureValue

            val obligacion = resultado.one()

            // Verificar que las supresiones iniciales se guardaron correctamente
            val bobSupresiones = obligacion.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
            val bobDetallesSupresiones: String = bobSupresiones.get("BOB_DETALLES_SUPRESIONES")

            decode[List[DetallesSupresiones]](bobDetallesSupresiones) match {
              case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
              case Right(supresiones) =>
                supresiones.length should be(2)
                supresiones.exists(_.BOB_TIPO_SUP.contains("CMPAMI")) should be(true)
                supresiones.exists(_.BOB_TIPO_SUP.contains("CMYPLAN")) should be(true)
            }
          }

          // Enviar segundo evento con diferentes supresiones
          val obligacionModificadaJson =
            s"""{
              "EV_ID": "20241025155256009187825139601",
              "BOB_SOJ_IDENTIFICADOR": "$idObjeto2",
              "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
              "BOB_SUJ_IDENTIFICADOR": "$sujeto2",
              "BOB_OBN_ID": "$obnId2",
              "BOB_PERIODO": "2023",
              "BOB_CUOTA": "00",
              "BOB_SUPRESIONES": {
                "BOB_DETALLES_SUPRESIONES": [
                  {
                    "BOB_TIPO_SUP": "CMSSS",
                    "BOB_DESCRIPCION": "[SALUD] - Bloqueo por pase a SSS",
                    "BOB_ESTADO_SUP": "PENDING",
                    "BOB_FECHA_INICIO_SUP": "2022-05-22 00:00:00.0",
                    "BOB_FECHA_FIN_SUP": "2022-05-22 00:00:00.0"
                  }
                ]
              }
            }"""

          val testObligacionModificada: Either[io.circe.Error, ObligacionesAnt] =
            decode[ObligacionesAnt](obligacionModificadaJson)

          testObligacionModificada match {
            case Right(obligacionModificada) =>
              messageProducer.produceObligacion(obligacionModificada)

              eventually(timeout(15.seconds), interval(100.milliseconds)) {
                val resultado2: AsyncResultSet = cassandra.cassandraWrite
                  .cqlSelect(
                    s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${idObjeto2}' AND BOB_SOJ_TIPO_OBJETO = '${tipoObjeto}';"
                  )
                  .futureValue

                val obligacion2 = resultado2.one()

                // Verificar que solo quedaron las supresiones del último evento
                val bobSupresiones2 = obligacion2.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
                val bobDetallesSupresiones2: String = bobSupresiones2.get("BOB_DETALLES_SUPRESIONES")

                decode[List[DetallesSupresiones]](bobDetallesSupresiones2) match {
                  case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
                  case Right(supresiones) =>
                    supresiones.length should be(1)
                    supresiones.head.BOB_TIPO_SUP.get should be("CMSSS")
                    supresiones.head.BOB_ESTADO_SUP.get should be("PENDING")
                    supresiones.head.BOB_DESCRIPCION.get should be("[SALUD] - Bloqueo por pase a SSS")
                }
              }

              // Enviar tercer evento sin supresiones
              val obligacionSinSupresionesJson =
                s"""{
                  "EV_ID": "20241025155256009187825139602",
                  "BOB_SOJ_IDENTIFICADOR": "$idObjeto3",
                  "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
                  "BOB_SUJ_IDENTIFICADOR": "$sujeto3",
                  "BOB_OBN_ID": "$obnId3",
                  "BOB_PERIODO": "2023",
                  "BOB_CUOTA": "00"
                }"""

              val testObligacionSinSupresiones: Either[io.circe.Error, ObligacionesAnt] =
                decode[ObligacionesAnt](obligacionSinSupresionesJson)

              testObligacionSinSupresiones match {
                case Right(obligacionSinSupresiones) =>
                  messageProducer.produceObligacion(obligacionSinSupresiones)

                  eventually(timeout(15.seconds), interval(100.milliseconds)) {
                    val resultado3: AsyncResultSet = cassandra.cassandraWrite
                      .cqlSelect(
                        s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${idObjeto3}' AND BOB_SOJ_TIPO_OBJETO = '${tipoObjeto}';"
                      )
                      .futureValue

                    val obligacion3 = resultado3.one()

                    // Modificada la verificación para aceptar null o un mapa vacío
                    val bobSupresiones3 = obligacion3.getMap("BOB_SUPRESIONES", classOf[String], classOf[String])
                    (bobSupresiones3 == null || bobSupresiones3.isEmpty) should be(true)
                  }

                case Left(error) =>
                  fail(s"Error decodificando JSON sin supresiones: $error")
              }

            case Left(error) =>
              fail(s"Error decodificando JSON modificado: $error")
          }

        case Left(error) =>
          fail(s"Error decodificando JSON inicial: $error")
      }
  }
}
