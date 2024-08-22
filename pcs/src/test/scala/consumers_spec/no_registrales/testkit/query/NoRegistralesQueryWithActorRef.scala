package consumers_spec.no_registrales.testkit.query

import akka.actor.ActorRef
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetStateObjeto
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetObjetoResponse
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.application.entities.ObligacionQueries.GetStateObligacion
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoQueries.GetStateSujeto
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoQueries.GetStateObjetoVinculo
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers_spec.no_registrales.testkit.MonitoringAndMessageProducerMock
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

class NoRegistralesQueryWithActorRef(sujeto: ActorRef, vinculoActor: ActorRef)
    extends NoRegistralesQueryTestKit
    with AgainstActors
    with ScalaFutures
    with IntegrationPatience {

  val actor: ActorRef = sujeto

  def getStateObligacion(obligacionExample: ObligacionExternalDto): GetObligacionResponse = {
    println("Consultando a :" + obligacionExample.BOB_OBN_ID)
    sujeto
      .ask[GetObligacionResponse](
        GetStateObligacion(
          obligacionExample.BOB_SUJ_IDENTIFICADOR,
          obligacionExample.BOB_SOJ_IDENTIFICADOR,
          obligacionExample.BOB_SOJ_TIPO_OBJETO,
          obligacionExample.BOB_OBN_ID
        )
      )
      .futureValue
  }

  def getStateObjeto(objetoExample: ObjetoExternalDto): GetObjetoResponse = {
    println("Consultando a :" + objetoExample.SOJ_IDENTIFICADOR)
    sujeto
      .ask[GetObjetoResponse](
        GetStateObjeto(
          objetoExample.SOJ_SUJ_IDENTIFICADOR,
          objetoExample.SOJ_IDENTIFICADOR,
          objetoExample.SOJ_TIPO_OBJETO
        )
      )
      .futureValue
  }
  def getStateSujeto(sujetoExample: SujetoExternalDto): GetSujetoResponse =
    sujeto
      .ask[GetSujetoResponse](
        GetStateSujeto(
          sujetoExample.SUJ_IDENTIFICADOR
        )
      )
      .futureValue
}
