package readside.proyectionists.registrales.plan_pago_detalles

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.PlanPagoRemovedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import org.slf4j.LoggerFactory
import consumers.registral.plan_pago_detalles.infrastructure.json.json._
import io.circe.parser._
import scala.concurrent.Future
import scala.util.{Failure, Success}

class PlanPagoRemovedFromDtoHandler (
                                      implicit
                                      r: MonitoringAndCassandraWrite
                                    ) extends ActorTransaction[PlanPagoRemovedFromDto](r.monitoring)(r.actorTransactionRequirements){
  private val log = LoggerFactory.getLogger(this.getClass)
  override def topic: String = "PlanPagoObnRemovedFromDto"

  override def topicRetry: String = "PlanPagoObnRemovedFromDto_retry"

  override def topicError: String = "PlanPagoObnRemovedFromDto_error"
  override def processInput(input: String): Either[Throwable, PlanPagoRemovedFromDto] = {
    decode[PlanPagoRemovedFromDto](input)
  }


  override def processMessage(registro: PlanPagoRemovedFromDto): Future[Response.SuccessProcessing] = {
    val cassandra = new CassandraWriteProduction()
    for {
      done <- cassandra
        .cql(
          s"""
    DELETE FROM read_side.buc_plan_pago_detalles """ +
            """ WHERE bpl_identificador = """ +
            s""" '${registro.planPagoId}' """ +
            s""" and bpd_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
            s""" and bpd_soj_identificador = '${registro.objetoId}' """ +
            s""" and bpd_obn_id = '${registro.obligacionId}' """
        )
        .andThen {
          case Failure(exception) => log.error("Dont persist plan_pago -1" + exception)
          case Success(_) => {
            log.debug("Persiste plan_pago -1 ")
          }
        }
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}
