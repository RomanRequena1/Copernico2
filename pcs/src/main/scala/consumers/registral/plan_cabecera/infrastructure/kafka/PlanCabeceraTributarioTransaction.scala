package consumers.registral.plan_cabecera.infrastructure.kafka

import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraCommands
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraExternalDto.PlanCabeceraTri
import consumers.registral.plan_cabecera.infrastructure.dependency_injection.PlanCabeceraActor
import consumers.registral.plan_cabecera.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaTypedTypedAsk
import io.circe.parser.decode
import monitoring.Monitoring
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class PlanCabeceraTributarioTransaction(actor: PlanCabeceraActor, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
  ) extends ActorTransaction[PlanCabeceraTri](monitoring) {
  private val log = LoggerFactory.getLogger(this.getClass)
    def topic = "DGR-COP-PLAN-CABECERA-TRI"

    def topicRetry = "DGR-COP-PLAN-CABECERA-TRI_retry"

    def topicError = "DGR-COP-PLAN-CABECERA-TRI_error"

    def processInput(input: String): Either[Throwable, PlanCabeceraTri] = {
      log.error("LOGGGGGGGGG"+decode[PlanCabeceraTri](input))
      decode[PlanCabeceraTri](input)
    }

    override def processMessage(registro: PlanCabeceraTri): Future[Response.SuccessProcessing] = {

      val command: PlanCabeceraCommands =
        if (registro.BPL_ESTADO.contains("BAJA")) {
          PlanCabeceraCommands.PlanCabeceraRemoveFromDto(
            deliveryId = registro.EV_ID,
            planCabeceraId = registro.BPL_IDENTIFICADOR,
            registro = registro
          )
        }
        else {
          log.error("DSAJSADSDAJSADAJSDAS"+registro)
          PlanCabeceraCommands.PlanCabeceraUpdateFromDto(
            deliveryId = registro.EV_ID,
            planCabeceraId = registro.BPL_IDENTIFICADOR,
            registro = registro
          )
        }
      actor.ask(command)
    }
}
