package consumers.no_registral.obligacion.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{DownObligacion, ObligacionRemove, ObligacionUpdateFromDto}
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{DetallesObligacion, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json._
import design_principles.actor_model.{Command, Response}
import monitoring.Monitoring
import play.api.libs.json.Reads
import serialization.maybeDecode

import scala.concurrent.Future
import scala.util.Try

case class ObligacionTributariaTransactionMultiobjeto(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesTri](monitoring) {

  /** Handles the deserialization of detalles de obligaciones tributarias */
  implicit val b: Reads[Seq[DetallesObligacion]] = Reads.seq(DetallesObligacionF.reads)

  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-OBLIGACIONES-TRI-M"

  def processInput(input: String): Either[Throwable, ObligacionesTri] =
    maybeDecode[ObligacionesTri](input)

  def processMessage(obligacion: ObligacionesTri): Future[Response.SuccessProcessing] = {
    val isAdheridoDebito = obligacion.BOB_ADHERIDO_DEBITO.contains("S")
    val command: Command = obligacion match {
      //this pattern match isn't  commutative
      case obn: ObligacionesTri if precondicionParaDarDeBaja(obn) =>
        DownObligacion(
          sujetoId = obn.BOB_SUJ_IDENTIFICADOR,
          objetoId = obn.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obn.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obn.BOB_OBN_ID,
          deliveryId = obn.EV_ID
        )
      case obn: ObligacionesTri if isNotDeuda(obn) =>
        ObligacionRemove(
          sujetoId = obn.BOB_SUJ_IDENTIFICADOR,
          objetoId = obn.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obn.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obn.BOB_OBN_ID // TODO consider adding here deliveryId, because this is the consequence of a Kafka message
        )
      case obn: ObligacionesTri =>
        ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = obligacion,
          //todo: fix
          detallesObligacion = extractOtrosAtributos(obligacion).getOrElse(Seq.empty),
          isAdheridoDebito = isAdheridoDebito
        )
    }

    //return a response  to the actorRef given, this case is an ActorRef of SujetoActor
    actorRef.ask[Response.SuccessProcessing](command)
  }

  def precondicionParaDarDeBaja(registro: ObligacionesTri): Boolean = registro.BOB_ESTADO.contains("BAJA")

  private def isNotDeuda(obligacion: ObligacionesTri): Boolean = {

    val otrosAtributos = extractOtrosAtributos(obligacion).getOrElse(default = Nil)

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

  private def extractOtrosAtributos(obn: ObligacionesTri) = {
    val detalles = for {
      otrosAtributos <- obn.BOB_OTROS_ATRIBUTOS
      bobDetalles <- (otrosAtributos \ "BOB_DETALLES").toOption
      detalles = serialization.decodeF[Seq[DetallesObligacion]](bobDetalles.toString)
    } yield (detalles)
    detalles
  }

  private def extractRuleNumber(otrosAtributos: Seq[DetallesObligacion]) = {
    otrosAtributos.headOption.flatMap(_.RULE_NUMBER)
  }


}
