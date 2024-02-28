package readside.proyectionists.registrales.plan_cabecera

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.PlanCabeceraRemovedFromDto
import consumers.registral.plan_cabecera.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PlanCabeceraRemovedFromDtoHandler (
                                      implicit
                                      r: MonitoringAndCassandraWrite
                                    ) extends ActorTransaction[PlanCabeceraRemovedFromDto](r.monitoring)(r.actorTransactionRequirements){
  private val log = LoggerFactory.getLogger(this.getClass)
  override def topic: String = "PlanCabeceraRemovedFromDto"

  override def topicRetry: String = "PlanCabeceraRemovedFromDto_retry"

  override def topicError: String = "PlanCabeceraRemovedFromDto_error"
  override def processInput(input: String): Either[Throwable, PlanCabeceraRemovedFromDto] = {
    decode[PlanCabeceraRemovedFromDto](input)
  }


  override def processMessage(registro: PlanCabeceraRemovedFromDto): Future[Response.SuccessProcessing] = {
    val cassandra = new CassandraWriteProduction()
    for {
      done <- cassandra
        .cql(
          s"""
    DELETE FROM read_side.buc_plan_cabecera """ +
            """ WHERE bpl_identificador = """ +
            s""" '${registro.planCabeceraId}' """
        )
        .andThen {
          case Failure(exception) => log.error("Dont persist plan_cabecera -1" + exception)
          case Success(_) => {
            log.debug("Persiste plan_cabecera -1 ")
          }
        }
      done <- cassandra
        .cql(
          s"""
    DELETE FROM read_side.buc_plan_pago_detalles """ +
            """ WHERE bpl_identificador = """ +
            s""" '${registro.planCabeceraId}' """
        )
        .andThen {
          case Failure(exception) => log.error("Dont persist plan_pago -1" + exception)
          case Success(_) => {
            log.debug("Persiste plan_pago -1 ")
          }
        }

    }yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}
