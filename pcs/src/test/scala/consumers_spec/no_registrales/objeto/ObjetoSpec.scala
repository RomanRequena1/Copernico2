package consumers_spec.no_registrales.objeto

import akka.actor.{ActorRef, ActorSystem}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.entities.ObjetoResponses
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.ObjetosTriDecoder
import consumers.no_registral.tranferencia.domain.{Vinculo, VinculoCotitular}
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers_spec.no_registrales.testkit.{MonitoringAndMessageProducerMock, NoRegistralesImplicitConversions}
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
      declaredFields
    }
   */

//  val examples = new Examples("ObjetoSpec")

  val jsonDiego = s"""{
        "EV_ID" : $deliveryIdAct,
        "SOJ_SUJ_IDENTIFICADOR" : "Diego",
        "SOJ_IDENTIFICADOR" : "AutoDiego",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Foca",
        "SOJ_ESTADO" : null
        }"""

  val jsonRoman = s"""{
        "EV_ID": $deliveryIdAct,
        "SOJ_SUJ_IDENTIFICADOR" : "Roman",
        "SOJ_IDENTIFICADOR" : "AutoJulian",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Escupe Fuego",
        "SOJ_TITULARIDAD" : "CONDOMINO",
        "SOJ_ESTADO" : null
        }"""

  val jsonJulian = s"""{
        "EV_ID": $deliveryIdAct,
        "SOJ_SUJ_IDENTIFICADOR" : "Julia",
        "SOJ_IDENTIFICADOR" : "AutoJulian",
        "SOJ_TIPO_OBJETO" : "A",
        "SOJ_DESCRIPCION" : "Escupe Fuego",
        "SOJ_TITULARIDAD" : "CONDOMINO",
        "SOJ_ESTADO" : null
        }"""

  val jsonLucas_R = s"""{
        "EV_ID": $deliveryIdAct,
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

  "Un objeto" should "1: crear vinculo sujeto objeto Diego" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)

    val messageProducer = context.messageProducer

    val Query = context.Query

  }

}
