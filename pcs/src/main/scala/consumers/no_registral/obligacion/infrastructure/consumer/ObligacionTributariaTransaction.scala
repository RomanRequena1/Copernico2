package consumers.no_registral.obligacion.infrastructure.consumer

import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoCommands._
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.application.entities.ObligacionCommands._
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.{DetallesObligacion, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.json._
import design_principles.actor_model.{Command, Response}
import monitoring.Monitoring
import play.api.libs.json.Reads
import serialization.maybeDecode

import scala.concurrent.Future
import scala.util.Try

case class ObligacionTributariaTransaction(actorRef: ActorRef, monitoring: Monitoring)(
  implicit
  actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObligacionesTri](monitoring) {

  /** Handles the deserialization of detalles de obligaciones tributarias */
  implicit val b: Reads[Seq[DetallesObligacion]] = Reads.seq(DetallesObligacionF.reads)

  def topic =
    Try {
      actorTransactionRequirements.config.getString(s"consumers.$simpleName.topic")
    } getOrElse "DGR-COP-OBLIGACIONES-TRI"

  def processInput(input: String): Either[Throwable, ObligacionesTri] =
    maybeDecode[ObligacionesTri](input)

  def processMessage(obligacion: ObligacionesTri): Future[Response.SuccessProcessing] = {
    val command: Command =  obligacion match {
       //todo el patternmatch no es conmutativo
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
          // RULE OF THUMB:
          // Command that is used in Kafka Transaction, is command that is going to use deliveryid
          )

      //Base case
      case obn: ObligacionesTri =>
         ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = obligacion,
          detallesObligacion = extractOtrosAtributos(obligacion).getOrElse(Seq.empty)
        )
    }

    //return a response  to the actorRef given, this case is an ActorRef of SujetoActor
    actorRef.ask[Response.SuccessProcessing](command)
  }

  def precondicionParaDarDeBaja(registro: ObligacionesTri): Boolean = registro.BOB_ESTADO.contains("BAJA")

  private def isNotDeuda(obligacion: ObligacionesTri): Boolean = {

    val otrosAtributos = extractOtrosAtributos(obligacion).getOrElse(default = Nil)

    val result = (if (otrosAtributos.nonEmpty) {

      val ruleNumber = extractRuleNumber(otrosAtributos)

      if (ruleNumber.contains("-1")){
        true
      }else {
        false
      }
    }
    else {
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

/*
  {
  "EV_ID" : "1044799163",
  "BOB_SUJ_IDENTIFICADOR" : "23-07972932-9",
  "BOB_SOJ_TIPO_OBJETO" : "I",
  "BOB_SOJ_IDENTIFICADOR" : "110121931761",
  "BOB_OBN_ID" : "20210000000027758779",
  "BOB_SALDO" : "336.87",
  "BOB_CUOTA" : "8",
  "BOB_ESTADO" : "ADMINISTRATIVA",
  "BOB_FISCALIZADA" : "N",
  "BOB_INDICE_INT_PUNIT" : "",
  "BOB_INDICE_INT_RESAR" : "",
  "BOB_INTERES_PUNIT" : "",
  "BOB_INTERES_RESAR" : "",
  "BOB_JUI_ID" : "",
  "BOB_PERIODO" : "2021",
  "BOB_PLN_ID" : "",
  "BOB_PRORROGA" : "2021-09-10 00:00:00.0",
  "BOB_TIPO" : "tributaria",
  "BOB_TOTAL" : "",
  "BOB_VENCIMIENTO" : "2021-09-10 00:00:00.0",
  "BOB_CAPITAL" : "336.87",
  "BOB_CONCEPTO" : "101",
  "BOB_IMPUESTO" : "5",
  "FECHA_BAJA" : "",
  "BOB_OTROS_ATRIBUTOS" : {
    "BOB_DETALLES" : [ {
      "EVO_OBN_PEO_ID_MATERIAL" : "DEB",
      "BOB_MUNICIPIO" : null,
      "JUICIO_MULTIOBJETO" : "N",
      "RULE_NUMBER" : "-1",
      "EVO_OBN_PEO_ID_FORMAL" : "NC",
      "PLAN_MULTIOBJETO" : "N"
    } ]
  }
}

 mensuales - anuales

si paga al menos 1 cuot del "mensual" eso hace que se de de baja esa  obligacion "super"
un plan de pago nace por obligaciones mensuales vencidas.
si opto por la anual, osea jamas pague ninguna cuota de ninguna mensual.
*/