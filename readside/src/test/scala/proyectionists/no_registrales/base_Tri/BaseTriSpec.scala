package proyectionists.no_registrales.base_Tri

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ObjetosTri}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.{DetallesObjetoDecoder, ObjetosTriDecoder}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.{
  DetallesObligacionDecoder,
  ObligacionesTriDecoder
}
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits._
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ObligacionesTri}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.SujetoTri
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import proyectionists.no_registrales.testkit.MessageTestkitUtils._
import proyectionists.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import utils.generators.Model.deliveryIdAct
import io.circe.parser.decode
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

  //FIXME: Validar que el serializador pueda detectar el tipo de datos sin comillas, ejemplo: 999 vs "999"
  //El sujeto funciona con 999 y "999"

  // - Verifica que primer evento con reglas de borrado no persistan y sin campos soj_otros_atributos. Json
  // - Verifica que el segundo evento con reglas de borrado borre y sin campos soj_otros_atributos. Json
  // - Verifica que evento modificado cambie campos con valores, mantenga valores de state. CaseClass
  // y borre los definidos.
  // - Verifica la baja del objeto. CaseClass

  // **Nota: validar String, LocalDateTime y BigDecimal

  "Test 1: Un sujeto Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val sujeto = "CuitTri-T1"
    /*
     El evento debe:
    - Persistir el mail y denominacion
    - No persistir direccion ni saldo
     */
    val sujetoJsonParcial =
      s"""
    {
      "EV_ID": $deliveryIdAct,
      "SUJ_IDENTIFICADOR": "$sujeto",
      "SUJ_DENOMINACION": "Sujeto-Test1",
      "SUJ_EMAIL": "test1@ejemplo.com",
      "SUJ_DIRECCION": "null",
      "SUJ_CAT_SUJ_ID": "999"
    }
    """

    val testSujetoInicial: Either[io.circe.Error, SujetoTri] = decode[SujetoTri](sujetoJsonParcial)

    testSujetoInicial match {
      case Right(sujetoInicial) =>
        messageProducer.produceSujeto(sujetoInicial)

        eventually(timeout(15.seconds), interval(100.milliseconds)) {
          val resultado: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${sujetoInicial.SUJ_IDENTIFICADOR}';"
            )
            .futureValue

          val sujeto = resultado.one()

          // Persistir el mail y denominacion
          sujeto.getString("SUJ_IDENTIFICADOR") should be(sujetoInicial.SUJ_IDENTIFICADOR)
          sujeto.getString("SUJ_DENOMINACION") should be(sujetoInicial.SUJ_DENOMINACION.get)
          sujeto.getString("SUJ_EMAIL") should be(sujetoInicial.SUJ_EMAIL.get)

          // No persistir direccion ni saldo
          sujeto.getInt("SUJ_CAT_SUJ_ID") should be(0)
          sujeto.isNull("SUJ_DIRECCION") should be(true)
        }

        /*
         El evento debe:
        - Persistir nueva direccion y canal origen
        - Eliminar el email
        - Mantener la denominacion
         */
        val sujetoModificadoJson =
          s"""
        {
          "EV_ID": $deliveryIdAct,
          "SUJ_IDENTIFICADOR": "$sujeto",
          "SUJ_DIRECCION": "Calle falsa 123",
          "SUJ_EMAIL": "null",
          "SUJ_CANAL_ORIGEN": "OTAX"
        }
        """

        val testSujetoModificado: Either[io.circe.Error, SujetoTri] = decode[SujetoTri](sujetoModificadoJson)

        testSujetoModificado match {
          case Right(sujetoModificado) =>
            messageProducer.produceSujeto(sujetoModificado)

            eventually(timeout(15.seconds), interval(100.milliseconds)) {
              val resultado: AsyncResultSet = cassandra.cassandraWrite
                .cqlSelect(
                  s"SELECT * FROM read_side.buc_sujeto WHERE SUJ_IDENTIFICADOR = '${sujetoModificado.SUJ_IDENTIFICADOR}';"
                )
                .futureValue

              val sujeto = resultado.one()
              //Mantener la denominacion
              sujeto.getString("SUJ_DENOMINACION") should be(sujetoInicial.SUJ_DENOMINACION.get)

              //Persistir nueva direccion y canal origen
              sujeto.getString("SUJ_IDENTIFICADOR") should be(sujetoModificado.SUJ_IDENTIFICADOR)
              sujeto.getString("SUJ_DIRECCION") should be(sujetoModificado.SUJ_DIRECCION.get)
              sujeto.getString("SUJ_CANAL_ORIGEN") should be(sujetoModificado.SUJ_CANAL_ORIGEN.get)

              // Eliminar el email
              sujeto.isNull("SUJ_EMAIL") should be(true)
            }

          case Left(error) =>
            println(s"Error decodificando JSON modificado: $error")
        }

      case Left(error) =>
        println(s"Error decodificando JSON inicial: $error")
    }
  }
  "Test 2: un objeto Tri" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
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

    val testObjetoParcial: Either[io.circe.Error, ObjetosTri] = decode[ObjetosTri](objetoJsonInicial)

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

          val testObjetoModificado: Either[io.circe.Error, ObjetosTri] = decode[ObjetosTri](objetoModificadoJson)

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

  "Test 3: un obligacion con state parcial" should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val cassandra = context.cassandra

      val sujeto = "CuitTri-T3"
      val IdObjeto = "ObjetoTri-T3"
      val tipoObjeto = "A"
      val obn_id = "1234T3"

      //bob_cuota, bob_vencimiento, tipo son optional pero son requeridos por el dmn y si es asi

      val obligacionJsonInicial =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto",
      "BOB_OBN_ID": "$obn_id",
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

      val testObligacionParcial: Either[io.circe.Error, ObligacionesTri] =
        decode[ObligacionesTri](obligacionJsonInicial)

      testObligacionParcial match {
        case Right(obligacionInicial) =>
          messageProducer.produceObligacion(obligacionInicial)

          eventually(timeout(15.seconds), interval(100.milliseconds)) {
            val resultado: AsyncResultSet = cassandra.cassandraWrite
              .cqlSelect(
                s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${IdObjeto}' AND BOB_SOJ_TIPO_OBJETO = '${tipoObjeto}';"
              )
              .futureValue

            val obligacion = resultado.one()

            // Persistir la BOB_SALDO, BOB_ESTADO, BOB_FISCALIZADA y BOB_VENCIMIENTO
            obligacion.getString("BOB_SOJ_IDENTIFICADOR") should be(obligacionInicial.BOB_SOJ_IDENTIFICADOR)
            obligacion.getString("BOB_ESTADO") should be(obligacionInicial.BOB_ESTADO.get)
            obligacion.getLocalDate("BOB_VENCIMIENTO").atStartOfDay() should be(obligacionInicial.BOB_VENCIMIENTO.get)
            obligacion.getFloat("BOB_SALDO").toInt should be(obligacionInicial.BOB_SALDO.get)
            obligacion.getString("BOB_FISCALIZADA") should be(obligacionInicial.BOB_FISCALIZADA.get)

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

            //          decode[List[DetallesSupresiones]](bobDetallesSupresiones) match {
            //            case Left(error) => fail(s"Error decoding BOB_DETALLES_SUPRESIONES: $error")
            //            case Right(detalles) =>
            //              // Persistir en soj_detalle el BOB_TIPO_SUP
            //              detalles.head.BOB_TIPO_SUP.get should be("R")
            //          }

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
            "BOB_PRORROGA": "2024-03-01 00:00:00.0",
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

            val testObligacionModificado: Either[io.circe.Error, ObligacionesTri] =
              decode[ObligacionesTri](obligacionModificadoJson)

            testObligacionModificado match {
              case Right(obligacionModificado) =>
                messageProducer.produceObligacion(obligacionModificado)

                eventually(timeout(15.seconds), interval(100.milliseconds)) {
                  val resultado: AsyncResultSet = cassandra.cassandraWrite
                    .cqlSelect(
                      s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '${IdObjeto}' AND BOB_SOJ_TIPO_OBJETO = '${tipoObjeto}';"
                    )
                    .futureValue

                  val obligacion2 = resultado.one()

                  // Mantener la bob_estado y bob_saldo
                  obligacion2.getString("BOB_ESTADO") should be(obligacionInicial.BOB_ESTADO.get)
                  obligacion2.getFloat("BOB_SALDO").toInt should be(obligacionInicial.BOB_SALDO.get)

                  // Persistir nuevo BOB_FISCALIZADA y BOB_PRORROGA
                  obligacion2.getString("BOB_FISCALIZADA") should be(obligacionModificado.BOB_FISCALIZADA.get)
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
                }

              case Left(error) =>
                println(s"Error decodificando JSON modificado: $error")
            }

          }

        case Left(error) =>
          println(s"Error decodificando JSON inicial: $error")
      }
  }


  "Test del bug de objeto vinculo" should "End to end, PCS a Readside" in parallelActorSystemRunner { implicit s =>
    implicit val dispatcher = s.dispatcher
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val cassandra = context.cassandra

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

    val sujeto = "SUJETO_30_TEST1"
    val sujeto2 = "SUJETO2_30_TEST1"
    val tipoObjeto = "A"
    val IdObjeto = "OBJETO30_PRIMERO"
    val IdObjeto2 = "OBJETO30_SEGUNDO"

    val obn_id3 = "obnDeuda-T1"

    val vtoVencidaParsed = LocalDateTime.now().minusDays(25).format(formatter)

    val fecha = LocalDateTime.now().minusDays(5)

    val periodo = fecha.getYear
    val cuota = fecha.getMonthValue

    /*
     El evento debe:
     Persistir el alta Suj1-Obj1-Tipo2
     El primer evento deberia persistir en True
     El segundo evento deberia persistir en False(alta de deuda)
     El tercer evento deberia persistir en True (pago de deuda)
     */

    //todo ALTA DE OBJ2 - TIPO2
    val SegundoObjeto30Porciento =
      s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "$sujeto",
      "SOJ_TIPO_OBJETO": "$tipoObjeto",
      "SOJ_IDENTIFICADOR": "$IdObjeto",
      "SOJ_DESCRIPCION": "SegundoObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

    //todo ALTA DE OBJ3 - TIPO2
    val TerceroObjeto30Porciento =
      s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "$sujeto2",
      "SOJ_TIPO_OBJETO": "$tipoObjeto",
      "SOJ_IDENTIFICADOR": "$IdObjeto",
      "SOJ_DESCRIPCION": "SegundoObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

    // cuando le meto deuda debe tener tiene 30 en false y el obj vinculo en false
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

    val pagoObligacion =
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
      "RULE_NUMBER": "-1"
      }]}
    }"""

    val obligacionVencida2 =
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

    val testSegundoObjeto30Porciento: Either[io.circe.Error, ObjetosTri] =
      decode[ObjetosTri](SegundoObjeto30Porciento)
    val testTercerObjeto30Porciento: Either[io.circe.Error, ObjetosTri] = decode[ObjetosTri](TerceroObjeto30Porciento)
    val testObligacionVencida: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionVencida)
    val testObligacionPagada: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](pagoObligacion)
    val testObligacionVencida2: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionVencida2)


    testSegundoObjeto30Porciento match {
      case Right(segundoObjeto) =>
        messageProducer.produceObjeto(segundoObjeto)
        eventually(timeout(15.seconds), interval(100.milliseconds)) {
          //Objeto1
          val resultadoObjeto1: AsyncResultSet = cassandra.cassandraWrite
            .cqlSelect(
              s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
            )
            .futureValue

          val objeto1 = resultadoObjeto1.one()

          // Validar la aplicabilidad del cupon: soj_aplicarDescuento
          objeto1.getBoolean("soj_aplicarDescuento") should be(true)

          objeto1.getBoolean("soj_tiene30Objeto") should be(true)
          //Fixme: verificar pq el objeto_vinculo no persiste en su map el primer VSO
          // objeto2.getBoolean("soj_tiene30ObjetoVinculo") should be(false)

        }

        testTercerObjeto30Porciento match {
          case Right(tercerObjeto) =>
            messageProducer.produceObjeto(tercerObjeto)

            eventually(timeout(15.seconds), interval(100.milliseconds)) {

              //Objeto 2
              val resultadoObjeto2: AsyncResultSet = cassandra.cassandraWrite
                .cqlSelect(
                  s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto2' AND SOJ_IDENTIFICADOR = '$IdObjeto' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                )
                .futureValue

              val objeto2 = resultadoObjeto2.one()

              // Validar la aplicabilidad del cupon: soj_aplicarDescuento
              objeto2.getBoolean("soj_aplicarDescuento") should be(true)
              objeto2.getBoolean("soj_tiene30Objeto") should be(true)
            }

            testObligacionVencida match {
              case Right(obnVencida) =>
                messageProducer.produceObligacion(obnVencida)

                eventually(timeout(15.seconds), interval(100.milliseconds)) {
                  //Obligacion 3 DEUDA
                  val resultadoObligacion3: AsyncResultSet = cassandra.cassandraWrite
                    .cqlSelect(
                      s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' " +
                      s" AND BOB_PERIODO = '$periodo' AND BOB_CUOTA = '$cuota' AND BOB_OBN_ID = '$obn_id3';"
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

                  //Objeto 2
                  val resultadoObjeto2: AsyncResultSet = cassandra.cassandraWrite
                    .cqlSelect(
                      s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto2' AND SOJ_IDENTIFICADOR = '$IdObjeto' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
                    )
                    .futureValue

                  val objeto2 = resultadoObjeto2.one()

                  // Validar la aplicabilidad del cupon: soj_aplicarDescuento
                  objeto2.getBoolean("soj_aplicarDescuento") should be(true)
                  objeto2.getBoolean("soj_tiene30Objeto") should be(true)
                }

                testObligacionPagada match {
                  case Right(obnPagada) =>
                    messageProducer.produceObligacion(obnPagada)

                    eventually(timeout(15.seconds), interval(100.milliseconds)) {

                      val resultadoObnPaga: AsyncResultSet = cassandra.cassandraWrite
                        .cqlSelect(
                          s"SELECT count(*) FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' " +
                          s" AND BOB_PERIODO = '$periodo' AND BOB_CUOTA = '$cuota' AND BOB_OBN_ID = '$obn_id3';"
                        )
                        .futureValue

                      val obligacionPaga = resultadoObnPaga.one()

                      //validad la evaluacion de que esta pagada
                      obligacionPaga.getLong("count") should be(0)
                    }
                    testObligacionVencida2 match {
                      case Right(obnVencida) =>
                        messageProducer.produceObligacion(obnVencida)

                        eventually(timeout(15.seconds), interval(100.milliseconds)) {
                          //Obligacion 3 DEUDA
                          val resultadoObligacion3: AsyncResultSet = cassandra.cassandraWrite
                            .cqlSelect(
                              s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' " +
                                s" AND BOB_PERIODO = '$periodo' AND BOB_CUOTA = '$cuota' AND BOB_OBN_ID = '$obn_id3';"
                            )
                            .futureValue

                          val obligacion3 = resultadoObligacion3.one()

                          //Validar la evaluacion de la obligacion: resultdmn
                          obligacion3.getString("BOB_RESULTDMN") should be("-8")
                        }

                      case Left(error) =>
                        println(s"ERROR EN LA OBN PAGA: $error")
                    }
                    case Left(error) =>
                    println(s"ERROR EN LA OBN PAGA: $error")
                }
              case Left(error) =>
                println(s"ERROR EN LA OBN VENCIDA: $error")
            }
          case Left(error) =>
            println(s"ERROR EN EL TERCER OBJETO: $error")

        }
      case Left(error) =>
        println(s"ERROR EN EL SEGUNDO OBJETO: $error")
    }
  }


  "Test del bug del objeto_vinculo " should "End to end, PCS a Readside" in parallelActorSystemRunner {
    implicit s =>
      implicit val dispatcher = s.dispatcher
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val cassandra = context.cassandra

      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

      val sujeto = "SUJETO_30_TEST1"
      val tipoObjeto = "A"
      val IdObjeto3 = "OBJETO30_TERCERO"
      val obn_id3 = "obnDeuda-T1"

      val fecha = LocalDateTime.now().minusDays(5)
      val vtoPlazoGracia = LocalDateTime.now().minusDays(5).format(formatter)

      val periodo = fecha.getYear
      val cuota = fecha.getMonthValue

      val Objeto =
        s"""
      {
      "EV_ID": "$deliveryIdAct",
      "SOJ_SUJ_IDENTIFICADOR": "$sujeto",
      "SOJ_TIPO_OBJETO": "$tipoObjeto",
      "SOJ_IDENTIFICADOR": "$IdObjeto3",
      "SOJ_DESCRIPCION": "SegundoObjetoPrueba_T1",
      "SOJ_ESTADO": null,
      "SOJ_FECHA_INICIO": "2024-01-01 00:00:00.0"
      }
    """

      val obligacionVencidaNoDeuda =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto3",
      "BOB_OBN_ID": "$obn_id3",
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

      val obligacionVencida2 =
        s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto3",
      "BOB_OBN_ID": "$obn_id3",
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

      val obligacionPaga =
      s"""{
      "EV_ID": "$deliveryIdAct",
      "BOB_SUJ_IDENTIFICADOR": "$sujeto",
      "BOB_SOJ_TIPO_OBJETO": "$tipoObjeto",
      "BOB_SOJ_IDENTIFICADOR": "$IdObjeto3",
      "BOB_OBN_ID": "$obn_id3",
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
      "BOB_MUNICIPIO": "Municipio",
      "RULE_NUMBER": "-1"
      }]}
    }"""

      val testObjeto: Either[io.circe.Error, ObjetosTri] = decode[ObjetosTri](Objeto)
      val testObligacionVencida: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionVencidaNoDeuda)
      val testDeuda: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionVencida2)
      val testObligacionPaga: Either[io.circe.Error, ObligacionesTri] = decode[ObligacionesTri](obligacionPaga)


//      testObjeto match {
//        case Right(objetoprimero) =>
//
//          messageProducer.produceObjeto(objetoprimero)
//
//          eventually(timeout(15.seconds), interval(100.milliseconds)) {
//
//            val resultadoObjeto: AsyncResultSet = cassandra.cassandraWrite
//              .cqlSelect(
//                s"SELECT * FROM read_side.buc_sujeto_objeto WHERE SOJ_SUJ_IDENTIFICADOR = '$sujeto' AND SOJ_IDENTIFICADOR = '$IdObjeto3' and SOJ_TIPO_OBJETO = '$tipoObjeto';"
//              )
//              .futureValue

            //val objeto = resultadoObjeto.one()

            //validad la evaluacion de que esta pagada
            //objeto.getString("BOB_TIPO") should be("tributaria")


            testObligacionVencida match {
              case Right(obnVencida) =>
                messageProducer.produceObligacion(obnVencida)

                eventually(timeout(15.seconds), interval(100.milliseconds)) {

                  val resultadoObnvencida: AsyncResultSet = cassandra.cassandraWrite
                    .cqlSelect(
                      s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto3' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' " +
                        s" AND BOB_PERIODO = '$periodo' AND BOB_CUOTA = '$cuota' AND BOB_OBN_ID = '$obn_id3';"
                    )
                    .futureValue

                  val obligacionVencida = resultadoObnvencida.one()

                  //validad la evaluacion de que esta pagada
                  obligacionVencida.getString("BOB_TIPO") should be("tributaria")

                  testDeuda match {
                    case Right(obnDeuda) =>
                      messageProducer.produceObligacion(obnDeuda)

                      eventually(timeout(15.seconds), interval(100.milliseconds)) {

                        val resultadoDeuda: AsyncResultSet = cassandra.cassandraWrite
                          .cqlSelect(
                            s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto3' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' " +
                              s" AND BOB_PERIODO = '$periodo' AND BOB_CUOTA = '$cuota' AND BOB_OBN_ID = '$obn_id3';"
                          )
                          .futureValue

                        val obligacionDeuda = resultadoDeuda.one()

                        //validad la evaluacion de que esta pagada
                        obligacionVencida.getString("BOB_TIPO") should be("tributaria")
                      }

                      testObligacionPaga match {
                        case Right(obnPaga) =>
                          messageProducer.produceObligacion(obnPaga)

                          eventually(timeout(15.seconds), interval(100.milliseconds)) {

                            val resultadoPaga: AsyncResultSet = cassandra.cassandraWrite
                              .cqlSelect(
                                s"SELECT * FROM read_side.buc_obligaciones WHERE BOB_SOJ_IDENTIFICADOR = '$IdObjeto3' AND BOB_SOJ_TIPO_OBJETO = '$tipoObjeto' " +
                                  s" AND BOB_PERIODO = '$periodo' AND BOB_CUOTA = '$cuota' AND BOB_OBN_ID = '$obn_id3';"
                              )
                              .futureValue

                            val obligacionVencida = resultadoPaga.one()

                            //validad la evaluacion de que esta pagada
                            obligacionVencida.getString("BOB_TIPO") should be("tributaria")
                          }
                        case Left(e) =>
                          println("ERROR DE LA OBN VENCIDA" + e)
                      }
                    case Left(e) =>
                      println("ERROR DE LA OBN VENCIDA" + e)
                  }
                }
              case Left(e) =>
                println("ERROR DE LA OBN VENCIDA" + e)
            }
          }
        //case Left(e) =>
          //println("ERROR DE LA OBN VENCIDA" + e)
      //}

  //}
}
