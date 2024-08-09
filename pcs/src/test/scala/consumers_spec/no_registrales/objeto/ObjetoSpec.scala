package consumers_spec.no_registrales.objeto

import akka.actor.{ActorRef, ActorSystem}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.entities.ObjetoResponses
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.ObjetosTriDecoder
import consumers.no_registral.tranferencia.domain.{Vinculo, VinculoCotitular}
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers_spec.no_registrales.testkit.{
  Examples,
  MonitoringAndMessageProducerMock,
  NoRegistralesImplicitConversions
}
import utils.generators.Model.{deliveryId, deliveryIdAct}
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import design_principles.actor_model.ActorSpec
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.{MessageProcessor, MessageProducer}
import consumers_spec.no_registrales.testkit.MessageTestkitUtils._
import io.circe.parser.decode

object ObjetoSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         Query: NoRegistralesQueryTestKit)
}
abstract class ObjetoSpec(
    getContext: ActorSystem => ObjetoSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {

  /*
   TODO!: Para extraer campos por si necesitamos a futuro en los Asserts
    def obtenerCamposAssert(event: ObjetosTri): Array[Field] = {
      val declaredFields: Array[Field] = event.getClass.getDeclaredFields
      println("AAAA" + declaredFields)
      declaredFields
    }
   */

  val examples = new Examples("ObjetoSpec")

  val jsonDiego = s"""{
        "EV_ID" : "${deliveryIdAct}",
        "SOJ_SUJ_IDENTIFICADOR" : "Diego",
        "SOJ_IDENTIFICADOR" : "AutoDiego",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Foca",
        "SOJ_ESTADO" : null}"""

  val jsonRoman = s"""{
        "EV_ID" : "${deliveryIdAct}",
        "SOJ_SUJ_IDENTIFICADOR" : "Roman",
        "SOJ_IDENTIFICADOR" : "AutoJulian",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Escupe Fuego",
        "SOJ_TITULARIDAD" : "CONDOMINO",
        "SOJ_ESTADO" : null
        }"""

  val jsonJulian = s"""{
        "EV_ID" : "${deliveryIdAct}",
        "SOJ_SUJ_IDENTIFICADOR" : "Julia",
        "SOJ_IDENTIFICADOR" : "AutoJulian",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Escupe Fuego",
        "SOJ_TITULARIDAD" : "CONDOMINO",
        "SOJ_ESTADO" : null
        }"""

  val jsonLucas_R = s"""{
        "EV_ID" : "${deliveryIdAct}",
        "SOJ_SUJ_IDENTIFICADOR" : "Lucas",
        "SOJ_IDENTIFICADOR" : "AutoJulian",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Escupe Fuego",
        "SOJ_TITULARIDAD" : "CONDOMINO",
        "SOJ_ESTADO" : null,
        "SOJ_OTROS_ATRIBUTOS" : {
          "SOJ_DETALLES" : [ {
            "RESPONSABLE_OTROS_ATRIBUTOS" : "S"
            } ]
          }
        }"""

  "Un objeto" should "1: crear vinculo sujeto objeto Diego" in parallelActorSystemRunner {
    implicit s =>
    val context = getContext(s)

    val messageProducer = context.messageProducer

    val Query = context.Query

    decode[ObjetosTri](jsonDiego) match {
      case Left(err) => println("Error decoding Json" + err)
      case Right(event) =>
        messageProducer.produceObjeto(event)
        eventually {
          val response: ObjetoResponses.GetObjetoResponse = Query.getStateObjeto(event)
          response.registro.get should be(event)
        }
    }
  }

  "Tres Objetos" should "3: crear VSO-Lucas(Responsable) y Julian, Roman co-titulares" in parallelActorSystemRunner {
    implicit s =>
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val objetoId = "AutoJulian"
//      val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(MonitoringAndMessageProducerMock.dummy)
      val expectedVinculo = Map(
        (Vinculo("Roman", "AutoJulian", "A"), VinculoCotitular(true, Some(false), Some("CONDOMINO"), None)),
        (Vinculo("Julia", "AutoJulian", "A"), VinculoCotitular(true, Some(false), Some("CONDOMINO"), None)),
        (Vinculo("Lucas", "AutoJulian", "A"), VinculoCotitular(true, Some(true), Some("CONDOMINO"), None))
      )
      val Query = context.Query
      decode[ObjetosTri](jsonRoman) match {
        case Left(err) => println("Error decoding Json Roman" + err)
        case Right(event) =>
          messageProducer.produceObjeto(event)
          eventually {
            val response: ObjetoResponses.GetObjetoResponse = Query.getStateObjeto(event)
            response.registro.get should be(event)
          }
      }
      decode[ObjetosTri](jsonJulian) match {
        case Left(err) => println("Error decoding Json Julian" + err)
        case Right(event) =>
          messageProducer.produceObjeto(event)
          eventually {
            val response: ObjetoResponses.GetObjetoResponse = Query.getStateObjeto(event)
            response.registro.get should be(event)
          }
      }
      decode[ObjetosTri](jsonLucas_R) match {
        case Left(err) => println("Error decoding Json Lucas" + err)
        case Right(event) =>
          messageProducer.produceObjeto(event)
          println("Evento Lucas: " + event)
          eventually {
            val response: ObjetoResponses.GetObjetoResponse = Query.getStateObjeto(event)
            response.registro.get should be(event)
          }
      }

      eventually {
        val stateObjetoVinculo = Query.getStateObjetoVinculo(objetoId)
        stateObjetoVinculo.mapVinculo should be(expectedVinculo)
      }
  }

//  "un objeto" should "5: mandar una exclusion a Roman" in parallelActorSystemRunner{
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      val evento = examples.ObjetoExampleConExclusionRoman
//
//      messageProducer.produceObjeto(evento)
//      eventually {
//        val response = Query.getStateObjeto(evento)
//        println("R:" + response.registro)
//        response.registro should be(Some(evento))
//        response.registro should be(Some(evento))
//        response.treintaFinal should be(true)
//      }
//  }
//
//  "un objeto" should "6: mandar una exclusion vencida a Roman" in parallelActorSystemRunner{
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      val evento = examples.ObjetoExampleConExclusionVencidaRoman
//
//      messageProducer.produceObjeto(evento)
//      eventually {
//        val response = Query.getStateObjeto(evento)
//        println("R:" + response.registro)
//        response.registro should be(Some(evento))
//        response.registro should be(Some(evento))
////        response.treintaFinal should be(Some(true))
//      }
//  }
//
//  "un objeto" should "7: mandar una exclusion a Diego" in parallelActorSystemRunner{
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      val evento = examples.ObjetoExampleConExclusionDiego
//
//      messageProducer.produceObjeto(evento)
//      eventually {
//        val response = Query.getStateObjeto(evento)
//        println("R:" + response.registro)
//        response.registro should be(Some(evento))
//        response.registro should be(Some(evento))
////        response.treintaFinal should be(Some(true))
//      }
//  }
  //  "un objeto" should "1: crear vinculo sujeto objeto Diego" in parallelActorSystemRunner {
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      messageProducer.produceObjeto(examples.objetoExample)
//      eventually {
//        val response = Query.getStateObjeto(examples.objetoExample)
//        println("R:" + response.registro)
//        response.registro should be(Some(examples.objetoExample))
//      }
//  }

//  "un objeto" should "2: crear vinculo sujeto objeto Roman" in parallelActorSystemRunner {
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      messageProducer.produceObjeto()
//
//  }
//
//  "un objeto" should "3: crear vinculo sujeto objeto Lucas (responsable)" in parallelActorSystemRunner {
//    implicit s =>
//      val context = getContext(s)
//      val messageProducer = context.messageProducer
//      val Query = context.Query
//      messageProducer.produceObjeto()
//
//  }
  /*"un objeto" should
  "pisar una obligacion con otra nueva que llegue desde Kafka para el mismo ID" in parallelActorSystemRunner {
    implicit s =>
      val context = getContext(s)
      val messageProducer = context.messageProducer
      val Query = context.Query
      messageProducer produceObligacion examples.obligacionWithSaldo200
      eventually {
        val response = Query getStateObjeto examples.obligacionWithSaldo200
        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
      }

      messageProducer produceObligacion examples.obligacionWithSaldo50
      eventually {
        val response = Query getStateObjeto examples.obligacionWithSaldo50
        response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      }
  }

  "un objeto" should
  "acumular saldo para diferentes obligaciones" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val Query = context.Query
    val anotherOne = examples.obligacionWithSaldo50.copy(
      BOB_OBN_ID = "anotherObligation",
      EV_ID = deliveryId
    )
    messageProducer produceObligacion examples.obligacionWithSaldo50
    messageProducer produceObligacion anotherOne
    eventually {
      val response = Query getStateObjeto examples.obligacionWithSaldo50
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO + anotherOne.BOB_SALDO)
    }
  }

//  "un objeto" should
//  "trasladar el estado baja a su state" in parallelActorSystemRunner { implicit s =>
//    val context = getContext(s)
//    val messageProducer = context.messageProducer
//    val Query = context.Query
//    messageProducer produceObjeto examples.objeto2
//    eventually {
//      val response = Query getStateObjeto examples.obligacionWithSaldo50
//      val isBaja: Boolean = isObjetoBajaFromGetObjetoResponse(response)
//      isBaja should be(false)
//    }
//    messageProducer produceObjeto examples.objeto2.copy(
//      SOJ_ESTADO = Some("BAJA")
//    )
//    eventually {
//      val response = Query getStateObjeto examples.obligacionWithSaldo50
//      val isBaja: Boolean = isObjetoBajaFromGetObjetoResponse(response)
//      isBaja should be(true)
//    }
//  }

  "un objeto" should
  "Restar saldo y eliminar una obligacion cuando la misma se da de baja" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)
    val messageProducer = context.messageProducer
    val Query = context.Query
    val obligacionBaja = examples.obligacionWithSaldo200.copy(BOB_OBN_ID = "obligationBaja")

    messageProducer produceObligacion obligacionBaja
    eventually {
      val response = Query getStateObjeto obligacionBaja
      response.saldo should be(obligacionBaja.BOB_SALDO)
    }

    messageProducer produceObligacion obligacionBaja.copy(BOB_ESTADO = Some("BAJA"))
    eventually {
      val response = Query getStateObjeto obligacionBaja
      response.saldo should be(0)
    }
  }*/

}
