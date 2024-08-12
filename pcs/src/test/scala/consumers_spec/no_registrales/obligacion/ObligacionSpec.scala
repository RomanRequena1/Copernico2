package consumers_spec.no_registrales.obligacion

import akka.actor.ActorSystem
import consumers.no_registral.obligacion.application.entities.{ObligacionResponses, ObligacionesAnt}
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.ObligacionesAntDecoder
import consumers_spec.no_registrales.objeto.ObjetoSpec
import consumers_spec.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import consumers_spec.no_registrales.testkit.MessageTestkitUtils._
import io.circe.parser.decode
import utils.generators.Model.deliveryIdAct

object ObligacionSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         Query: NoRegistralesQueryTestKit)
}
abstract class ObligacionSpec(
    getContext: ActorSystem => ObligacionSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {
  type AggregateRoot = String

//  val examples = new Examples("ObligacionSpec")

  val jsonAltaObligacionAntVigenteDiego2001 = s"""{
 "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "2001",
 "BOB_SALDO": "1000",
 "BOB_CUOTA": "1",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2024",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2024-12-21 00:00:00.0",
 "BOB_TIPO": "ANT",
 "BOB_TOTAL": "1000",
 "BOB_VENCIMIENTO": "2024-12-21 00:00:00.0",
 "BOB_CAPITAL": "1000",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2024-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ]
 },
 "BOB_SUPRESIONES": {
 "BOB_DETALLES_SUPRESIONES": [
   {
    "BOB_TIPO_SUP": "Tipo Supresion",
    "BOB_ESTADO_SUP": "Estado Supresion",
    "BOB_FECHA_INICIO_SUP": "2024-12-01 01:02:03.4",
    "BOB_FECHA_FIN_SUP": "2025-12-01 01:02:03.4"
   }
  ]
 }
}"""
  val jsonPagoObligacionAntVigenteDiego2001 = s"""{
 "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "2001",
 "BOB_SALDO": "1000",
 "BOB_CUOTA": "1",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2024",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2024-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "1000",
 "BOB_VENCIMIENTO": "2024-12-21 00:00:00.0",
 "BOB_CAPITAL": "1000",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2024-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "-1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ],
 "BOB_SUPRESIONES": {
 "BOB_DETALLES_SUPRESIONES": [
   {
    "BOB_TIPO_SUP": "Tipo Supresion",
    "BOB_ESTADO_SUP": "Estado Supresion",
    "BOB_FECHA_INICIO_SUP": "2024-12-01 01:02:03.4",
    "BOB_FECHA_FIN_SUP": "2025-12-01 01:02:03.4"
   }
  ]
 }
}"""

  val jsonAltaObligacionAntVencidaDiego801 = s"""{
 "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "801",
 "BOB_SALDO": "999",
 "BOB_CUOTA": "2",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2023",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2023-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "999",
 "BOB_VENCIMIENTO": "2023-12-21 00:00:00.0",
 "BOB_CAPITAL": "999",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2023-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ]
 }
 "BOB_SUPRESIONES": {
 "BOB_DETALLES_SUPRESIONES": [
   {
    "BOB_TIPO_SUP": "Tipo Supresion",
    "BOB_ESTADO_SUP": "Estado Supresion",
    "BOB_FECHA_INICIO_SUP": "2024-12-01 01:02:03.4",
    "BOB_FECHA_FIN_SUP": "2025-12-01 01:02:03.4"
   }
  ]
 }
}"""
  val jsonPagoObligacionAntVencidaDiego801 = s""" {
 "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "801",
 "BOB_SALDO": "999",
 "BOB_CUOTA": "2",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2023",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2023-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "999",
 "BOB_VENCIMIENTO": "2023-12-21 00:00:00.0",
 "BOB_CAPITAL": "999",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2023-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "-1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ]
 }
 "BOB_SUPRESIONES": {
 "BOB_DETALLES_SUPRESIONES": [
   {
    "BOB_TIPO_SUP": "Tipo Supresion",
    "BOB_ESTADO_SUP": "Estado Supresion",
    "BOB_FECHA_INICIO_SUP": "2024-12-01 01:02:03.4",
    "BOB_FECHA_FIN_SUP": "2025-12-01 01:02:03.4"
   }
  ]
 }
}"""

  val jsonAltaObligacionVigenteDiego1001 = s"""{
 "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "1001",
 "BOB_SALDO": "1000",
 "BOB_CUOTA": "1",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2024",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2024-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "1000",
 "BOB_VENCIMIENTO": "2024-12-21 00:00:00.0",
 "BOB_CAPITAL": "1000",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2024-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ],
}"""
  val jsonPagoObligacionVigenteDiego1001 = s"""{
 "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "1001",
 "BOB_SALDO": "1000",
 "BOB_CUOTA": "1",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2024",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2024-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "1000",
 "BOB_VENCIMIENTO": "2024-12-21 00:00:00.0",
 "BOB_CAPITAL": "1000",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2024-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "-1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ]
 }
}"""

  val jsonAltaObligacionAntVencidaDiego901 = s""" "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "901",
 "BOB_SALDO": "999",
 "BOB_CUOTA": "2",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2023",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2023-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "999",
 "BOB_VENCIMIENTO": "2023-12-21 00:00:00.0",
 "BOB_CAPITAL": "999",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2023-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ]
 }
}"""
  val jsonPagoObligacionAntVencidaDiego901 = s""" "EV_ID": "${deliveryIdAct}",
 "BOB_SUJ_IDENTIFICADOR": "Diego",
 "BOB_SOJ_TIPO_OBJETO": "A",
 "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
 "BOB_OBN_ID": "901",
 "BOB_SALDO": "999",
 "BOB_CUOTA": "2",
 "BOB_ESTADO": "ADMINISTRATIVA",
 "BOB_SUB_ESTADO": null,
 "BOB_CANAL_ORIGEN": "LOCAL",
 "BOB_FISCALIZADA": "N",
 "BOB_INDICE_INT_PUNIT": null,
 "BOB_INDICE_INT_RESAR": null,
 "BOB_INTERES_PUNIT": null,
 "BOB_INTERES_RESAR": null,
 "BOB_JUI_ID": null,
 "BOB_PERIODO": "2023",
 "BOB_PLN_ID": null,
 "BOB_PRORROGA": "2023-12-21 00:00:00.0",
 "BOB_TIPO": "tributaria",
 "BOB_TOTAL": "999",
 "BOB_VENCIMIENTO": "2023-12-21 00:00:00.0",
 "BOB_CAPITAL": "999",
 "BOB_CONCEPTO": "601",
 "BOB_IMPUESTO": "600",
 "FECHA_BAJA": null,
 "BOB_ADHERIDO_DEBITO": "N",
 "BOB_OGA_ID": "11800",
 "BOB_VENCIMIENTO_2": "2023-12-21 00:00:00.0",
 "SOJ_ID_EXTERNO": "1601876",
 "BOB_OTROS_ATRIBUTOS": {
 "BOB_DETALLES": [
   {
    "EVO_OBN_PEO_ID_MATERIAL": "PC",
    "BOB_MUNICIPIO": null,
    "BOB_INTERES_FINANCIACION": null,
    "JUICIO_MULTIOBJETO": "N",
    "RULE_NUMBER": "-1",
    "EVO_OBN_PEO_ID_FORMAL": "NC",
    "PLAN_MULTIOBJETO": "N",
    "BAND_30": true
   }
  ]
 }
}"""

//  "una obligacion" should
//  "pisar una obligacion con otra nueva que llegue desde Kafka para el mismo ID" in parallelActorSystemRunner {
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      messageProducer produceObligacion examples.obligacionWithSaldo200
//      eventually {
//        val response = Query getStateObligacion examples.obligacionWithSaldo200
//        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
//
//      }
//
//      messageProducer produceObligacion examples.obligacionWithSaldo50
//      eventually {
//        val response = Query getStateObligacion examples.obligacionWithSaldo50
//        response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
//      }
//
//  }
//
//  "una obligacion" should "4: dar de alta una obligacion vencida de Lucas" in parallelActorSystemRunner { implicit s =>
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val Query = context.Query
//    val evento = examples.obligacionExampleVencidaLucas
//    messageProducer.produceObligacion(evento)
//    eventually {
//      val response = Query.getStateObligacion(evento)
//      println("R" + response.registro)
//      response.registro should be(Some(evento))
//    }
//  }
//
//  "una obligacion" should "8: pagar la obligacion que tiene Lucas" in parallelActorSystemRunner { implicit s =>
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val Query = context.Query
//    val evento = examples.obligacionExamplePagaLucas
//    messageProducer.produceObligacion(evento)
//    eventually {
//      val response = Query.getStateObligacion(evento)
//      println("R" + response.registro)
//      response.registro should be(Some(evento))
//    }
//  }
//
//  "una obligacion" should
//  "eliminar una obligacion si llega otra nueva que llegue desde Kafka para el mismo ID y con el atributo estado con BAJA (-1)" in parallelActorSystemRunner {
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      messageProducer produceObligacion examples.obligacionWithSaldo200
//
//      eventually {
//        val response = Query getStateObligacion examples.obligacionWithSaldo200
//        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
//      }
//
//      messageProducer produceObligacion examples.obligacionWithSaldo200.copy(
//        BOB_ESTADO = Some("BAJA")
//      )
//
//      Thread.sleep(200)
//      eventually {
//        val response = Query getStateObligacion examples.obligacionWithSaldo200
//        response.saldo should be(0)
//      }
//      Thread.sleep(200)
//  }

  "una obligacion ANT" should "dar de alta una obligacion ANT" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val Query = context.Query
    decode[ObligacionesAnt](jsonAltaObligacionAntVigenteDiego2001) match {
      case Left(err) => println("Error decoding Json Diego" + err)
      case Right(event) =>
        messageProducer.produceObligacion(event)
        println("Evento Ant Diego: " + event)
        eventually {
          val response: ObligacionResponses.GetObligacionResponse = Query.getStateObligacion(event)
          println("Query Obn: " + response)
          response.registro.get should be(event)
        }
    }

  }

}
