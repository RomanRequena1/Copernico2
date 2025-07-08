//package consumers_spec.no_registrales.obligacion
//
//import akka.actor.ActorSystem
//import consumers.no_registral.obligacion.application.entities.{ObligacionResponses, ObligacionesAnt}
//import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.ObligacionesAntDecoder
//import consumers_spec.no_registrales.testkit.MessageTestkitUtils._
//import consumers_spec.no_registrales.testkit.NoRegistralesImplicitConversions
//import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
//import design_principles.actor_model.ActorSpec
//import design_principles.external_pub_sub.kafka.MessageProcessorLogging
//import io.circe.parser.decode
//import kafka.{MessageProcessor, MessageProducer}
//import utils.generators.Model.deliveryIdAct
//
//import java.time.LocalDateTime
//import java.time.format.DateTimeFormatter
//
//object ObligacionSpec {
//  case class TestContext(messageProducer: MessageProducer,
//                         messageProcessor: MessageProcessor with MessageProcessorLogging,
//                         Query: NoRegistralesQueryTestKit)
//}
//abstract class ObligacionSpec(
//    getContext: ActorSystem => ObligacionSpec.TestContext
//) extends ActorSpec
//    with NoRegistralesImplicitConversions {
//  type AggregateRoot = String
//
//  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
//
//  val fechaVigente = LocalDateTime.now().format(formatter)
//  val fechaVigenteParsed = LocalDateTime.parse(fechaVigente, formatter)
//  val periodoVigente = fechaVigenteParsed.getYear
//
//
//  val fechaVencida = LocalDateTime.now().minusYears(1).format(formatter)
//  val fechaVencidaParsed = LocalDateTime.parse(fechaVencida, formatter)
//  val periodoVencida = fechaVencidaParsed.minusYears(1).getYear
//
//  val fechaPlazoDeGracia = LocalDateTime.now().plusDays(9).format(formatter)
//
//  val fechaFutura = LocalDateTime.now().plusYears(1).format(formatter)
//  val fechaFuturaParsed = LocalDateTime.parse(fechaFutura, formatter)
//  val periodoFutura = fechaFuturaParsed.minusYears(1).getYear
//
//
//
//  val jsonAltaObligacionAntVigenteDiego2001 = s"""{
// "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "2001",
// "BOB_SALDO": "1000",
// "BOB_CUOTA": "1",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "$periodoVigente",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": null,
// "BOB_TIPO": "ANT",
// "BOB_TOTAL": "1000",
// "BOB_VENCIMIENTO": "$fechaVigente",
// "BOB_CAPITAL": "1000",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": null,
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// },
// "BOB_SUPRESIONES": {
// "BOB_DETALLES_SUPRESIONES": [
//   {
//    "BOB_TIPO_SUP": "Tipo Supresion",
//    "BOB_ESTADO_SUP": "Estado Supresion",
//    "BOB_FECHA_INICIO_SUP": "$fechaVencida",
//    "BOB_FECHA_FIN_SUP": "$fechaFutura"
//   }
//  ]
// }
//}"""
//  val jsonPagoObligacionAntVigenteDiego2001 = s"""{
// "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "2001",
// "BOB_SALDO": "1000",
// "BOB_CUOTA": "1",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "$periodoVigente",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": null,
// "BOB_TIPO": "ANT",
// "BOB_TOTAL": "1000",
// "BOB_VENCIMIENTO": "$fechaVigente",
// "BOB_CAPITAL": "1000",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": null,
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "-1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// },
// "BOB_SUPRESIONES": {
// "BOB_DETALLES_SUPRESIONES": [
//   {
//    "BOB_TIPO_SUP": "Tipo Supresion",
//    "BOB_ESTADO_SUP": "Estado Supresion",
//    "BOB_FECHA_INICIO_SUP": "$fechaVencida",
//    "BOB_FECHA_FIN_SUP": "$fechaFutura"
//   }
//  ]
// }
//}"""
//
//  val jsonAltaObligacionAntVencidaDiego801 = s"""{
// "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "2001",
// "BOB_SALDO": "1000",
// "BOB_CUOTA": "1",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "$periodoVencida",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": null,
// "BOB_TIPO": "ANT",
// "BOB_TOTAL": "1000",
// "BOB_VENCIMIENTO": "$fechaVencida",
// "BOB_CAPITAL": "1000",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": null,
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// },
// "BOB_SUPRESIONES": {
// "BOB_DETALLES_SUPRESIONES": [
//   {
//    "BOB_TIPO_SUP": "Tipo Supresion",
//    "BOB_ESTADO_SUP": "Estado Supresion",
//    "BOB_FECHA_INICIO_SUP": null,
//    "BOB_FECHA_FIN_SUP": null
//   }
//  ]
// }
//}"""
//  val jsonPagoObligacionAntVencidaDiego801 = s"""{
// "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "2001",
// "BOB_SALDO": "1000",
// "BOB_CUOTA": "1",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "$periodoVencida",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": null,
// "BOB_TIPO": "ANT",
// "BOB_TOTAL": "1000",
// "BOB_VENCIMIENTO": "$fechaVencida",
// "BOB_CAPITAL": "1000",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": null,
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "-1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// },
// "BOB_SUPRESIONES": {
// "BOB_DETALLES_SUPRESIONES": [
//   {
//    "BOB_TIPO_SUP": "Tipo Supresion",
//    "BOB_ESTADO_SUP": "Estado Supresion",
//    "BOB_FECHA_INICIO_SUP": null,
//    "BOB_FECHA_FIN_SUP": null
//   }
//  ]
// }
//}"""
//
//  //FIXME: corregir json de obligaciones tri
//  val jsonAltaObligacionVigenteDiego1001 = s"""{
// "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "1001",
// "BOB_SALDO": "1000",
// "BOB_CUOTA": "1",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "2024",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": "2024-12-21 00:00:00.0",
// "BOB_TIPO": "tributaria",
// "BOB_TOTAL": "1000",
// "BOB_VENCIMIENTO": "2024-12-21 00:00:00.0",
// "BOB_CAPITAL": "1000",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": "2024-12-21 00:00:00.0",
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ],
//}"""
//  val jsonPagoObligacionVigenteDiego1001 = s"""{
// "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "1001",
// "BOB_SALDO": "1000",
// "BOB_CUOTA": "1",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "2024",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": "2024-12-21 00:00:00.0",
// "BOB_TIPO": "tributaria",
// "BOB_TOTAL": "1000",
// "BOB_VENCIMIENTO": "2024-12-21 00:00:00.0",
// "BOB_CAPITAL": "1000",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": "2024-12-21 00:00:00.0",
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "-1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// }
//}"""
//
//  val jsonAltaObligacionAntVencidaDiego901 = s""" "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "901",
// "BOB_SALDO": "999",
// "BOB_CUOTA": "2",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "2023",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": "2023-12-21 00:00:00.0",
// "BOB_TIPO": "tributaria",
// "BOB_TOTAL": "999",
// "BOB_VENCIMIENTO": "2023-12-21 00:00:00.0",
// "BOB_CAPITAL": "999",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": "2023-12-21 00:00:00.0",
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// }
//}"""
//  val jsonPagoObligacionAntVencidaDiego901 = s""" "EV_ID": "${deliveryIdAct}",
// "BOB_SUJ_IDENTIFICADOR": "Diego",
// "BOB_SOJ_TIPO_OBJETO": "A",
// "BOB_SOJ_IDENTIFICADOR": "AutoDiego",
// "BOB_OBN_ID": "901",
// "BOB_SALDO": "999",
// "BOB_CUOTA": "2",
// "BOB_ESTADO": "ADMINISTRATIVA",
// "BOB_SUB_ESTADO": null,
// "BOB_CANAL_ORIGEN": "LOCAL",
// "BOB_FISCALIZADA": "N",
// "BOB_INDICE_INT_PUNIT": null,
// "BOB_INDICE_INT_RESAR": null,
// "BOB_INTERES_PUNIT": null,
// "BOB_INTERES_RESAR": null,
// "BOB_JUI_ID": null,
// "BOB_PERIODO": "2023",
// "BOB_PLN_ID": null,
// "BOB_PRORROGA": "2023-12-21 00:00:00.0",
// "BOB_TIPO": "tributaria",
// "BOB_TOTAL": "999",
// "BOB_VENCIMIENTO": "2023-12-21 00:00:00.0",
// "BOB_CAPITAL": "999",
// "BOB_CONCEPTO": "601",
// "BOB_IMPUESTO": "600",
// "FECHA_BAJA": null,
// "BOB_ADHERIDO_DEBITO": "N",
// "BOB_OGA_ID": "11800",
// "BOB_VENCIMIENTO_2": "2023-12-21 00:00:00.0",
// "SOJ_ID_EXTERNO": "1601876",
// "BOB_OTROS_ATRIBUTOS": {
// "BOB_DETALLES": [
//   {
//    "EVO_OBN_PEO_ID_MATERIAL": "PC",
//    "BOB_MUNICIPIO": null,
//    "BOB_INTERES_FINANCIACION": null,
//    "JUICIO_MULTIOBJETO": "N",
//    "RULE_NUMBER": "-1",
//    "EVO_OBN_PEO_ID_FORMAL": "NC",
//    "PLAN_MULTIOBJETO": "N",
//    "BAND_30": true
//   }
//  ]
// }
//}"""
//
//  "una obligacion ANT vigente" should "dar de alta una obligacion ANT vigente" in parallelActorSystemRunner { implicit s =>
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val Query = context.Query
//
//    decode[ObligacionesAnt](jsonAltaObligacionAntVigenteDiego2001) match {
//      case Left(err) => println("Error decoding Json Diego" + err)
//      case Right(event) =>
//        messageProducer.produceObligacion(event)
//        eventually {
//          val response: ObligacionResponses.GetObligacionResponse = Query.getStateObligacion(event)
//          response.registro.get should be(event)
//        }
//    }
//
//  }
//
//  "una obligacion ANT vencida" should "dar de alta una obligacion ANT vencida" in parallelActorSystemRunner { implicit s =>
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val Query = context.Query
//
//    decode[ObligacionesAnt](jsonAltaObligacionAntVencidaDiego801) match {
//      case Left(err) => println("Error decoding Json Diego" + err)
//      case Right(event) =>
//        messageProducer.produceObligacion(event)
//        eventually {
//          val response: ObligacionResponses.GetObligacionResponse = Query.getStateObligacion(event)
//          response.registro.get should be(event)
//        }
//    }
//  }
//
//}
