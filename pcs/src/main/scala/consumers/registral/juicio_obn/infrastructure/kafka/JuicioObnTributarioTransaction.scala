package consumers.registral.juicio_obn.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.{JuicioObnDeleteFromDto, JuicioObnUpdateFromDto}
import consumers.registral.juicio_obn.application.entities.{DetallesJuicioTri, JuicioObnTri}
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import design_principles.actor_model.Response
import monitoring.Monitoring
import serialization.maybeDecode
import consumers.registral.juicio_obn.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk

import scala.concurrent.Future

case class JuicioObnTributarioTransaction(actor: JuicioObnActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[JuicioObnTri](monitoring) {
  override def topic: String = "DGR-COP-JUICIOS-OBLIGACIONES-TRI"

  override def topicRetry: String = "DGR-COP-JUICIOS-OBLIGACIONES-TRI_retry"

  override def topicError: String = "DGR-COP-JUICIOS-OBLIGACIONES-TRI_error"

  override def processInput(input: String): Either[Throwable, JuicioObnTri] = {
    maybeDecode[JuicioObnTri](input)
  }



  override def processMessage(juicioObn: JuicioObnTri): Future[Response.SuccessProcessing] = {

    val command = juicioObn match {
      case obn: JuicioObnTri if isNotDeuda(obn) =>
        println("CUMBIA -1 ")
        JuicioObnDeleteFromDto(
        deliveryId = juicioObn.EV_ID,
        juicioObnId = juicioObn.BJU_IDENTIFICADOR,
        objetoId = juicioObn.BJD_SOJ_IDENTIFICADOR,
        tipoObjeto = juicioObn.BJD_SOJ_TIPO_OBJETO,
        obligacionId = juicioObn.BJD_OBN_ID,
        registro = juicioObn
      )
      case _ =>
        println("CUMBIA 1 ")
        JuicioObnUpdateFromDto(
        deliveryId = juicioObn.EV_ID,
        juicioObnId = juicioObn.BJU_IDENTIFICADOR,
        objetoId = juicioObn.BJD_SOJ_IDENTIFICADOR,
        tipoObjeto = juicioObn.BJD_SOJ_TIPO_OBJETO,
        obligacionId = juicioObn.BJD_OBN_ID,
        registro = juicioObn
      )
    }
    actor.ask(command)
  }

  private def isNotDeuda(juicioobn: JuicioObnTri): Boolean = {

    val otrosAtributos = extractOtrosAtributos(juicioobn).getOrElse(default = Nil)

    val result: Boolean = (if (otrosAtributos.nonEmpty) {

      val ruleNumber = extractRuleNumber(otrosAtributos)

      if (ruleNumber.contains("-1")) {
        true
      } else {
        false
      }
    } else {
      false
    })

    result
  }


  private def extractOtrosAtributos(obn: JuicioObnTri) = {
    val detalles = for {
      otrosAtributos <- obn.BJD_OTROS_ATRIBUTOS
      bobDetalles <- (otrosAtributos \ "BJD_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesJuicioTri]](bobDetalles.toString)
    } yield (detalles)
    detalles
  }

  private def extractRuleNumber(otrosAtributos: Seq[DetallesJuicioTri]) = {
    otrosAtributos.headOption.flatMap(_.RULE_NUMBER)
  }
}
