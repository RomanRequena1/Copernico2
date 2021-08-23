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

  //todo remove println: only for debug
  /*import java.time.format.DateTimeFormatter

  import java.time.ZonedDateTime
  val formatter = "%s ->[time = %s ,sujetoId = %s , objetoId = %s, tipoObjeto = %s, obligacionId = %s]"

  def getServerTime(): String = {
     DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(ZonedDateTime.now())
  }*/

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

        //todo remove println
        /*val console_debug_1 = formatter.format("ValidacionIsBajaObligacion"
          , getServerTime()
          , obn.BOB_SUJ_IDENTIFICADOR
          ,obn.BOB_SOJ_IDENTIFICADOR
          ,obn.BOB_SOJ_TIPO_OBJETO,obn.BOB_OBN_ID)

        println(console_debug_1)*/

        DownObligacion(
        sujetoId = obn.BOB_SUJ_IDENTIFICADOR,
        objetoId = obn.BOB_SOJ_IDENTIFICADOR,
        tipoObjeto = obn.BOB_SOJ_TIPO_OBJETO,
        obligacionId = obn.BOB_OBN_ID,
        deliveryId = obn.EV_ID
      )
      case obn: ObligacionesTri if isNotDeuda(obn) => {
        //todo remove println
        /*val console_debug_2 = formatter.format("ValidacionIsNotDeuda"
          , getServerTime()
          , obn.BOB_SUJ_IDENTIFICADOR
          ,obn.BOB_SOJ_IDENTIFICADOR
          ,obn.BOB_SOJ_TIPO_OBJETO,obn.BOB_OBN_ID)

        println(console_debug_2)*/

        ObligacionRemove(
          sujetoId = obn.BOB_SUJ_IDENTIFICADOR,
          objetoId = obn.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obn.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obn.BOB_OBN_ID // TODO consider adding here deliveryId, because this is the consequence of a Kafka message
          // RULE OF THUMB:
          // Command that is used in Kafka Transaction, is command that is going to use deliveryid
        )
      }
      //Base case
      case obn: ObligacionesTri =>
        /*val console_debug_3 = formatter.format("ValidacionIsCasoBaseObligacionUpdateFromDto"
          , getServerTime()
          , obn.BOB_SUJ_IDENTIFICADOR
          ,obn.BOB_SOJ_IDENTIFICADOR
          ,obn.BOB_SOJ_TIPO_OBJETO,obn.BOB_OBN_ID)

        println(console_debug_3)*/

         ObligacionUpdateFromDto(
          sujetoId = obligacion.BOB_SUJ_IDENTIFICADOR,
          objetoId = obligacion.BOB_SOJ_IDENTIFICADOR,
          tipoObjeto = obligacion.BOB_SOJ_TIPO_OBJETO,
          obligacionId = obligacion.BOB_OBN_ID,
          deliveryId = obligacion.EV_ID,
          registro = obligacion,
           //todo: fix

          detallesObligacion = extractOtrosAtributos(obligacion).getOrElse(Seq.empty)
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
