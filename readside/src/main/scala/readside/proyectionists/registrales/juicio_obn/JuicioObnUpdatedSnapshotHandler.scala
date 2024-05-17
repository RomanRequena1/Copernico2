package readside.proyectionists.registrales.juicio_obn

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.registral.juicio_obn.domain.JuicioObnEvents.JuicioObnUpdatedFromDto
import consumers.registral.juicio_obn.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.juicio_obn.projections.JuicioObnUpdatedFromDtoProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}
class JuicioObnUpdatedSnapshotHandler(
                                              implicit
                                              r: MonitoringAndCassandraWrite

                                            ) extends ActorTransaction[JuicioObnUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)
  override def topic: String = "JuicioObnUpdatedFronDto"

  override def topicRetry: String = "JuicioObnUpdatedFronDto_retry"

  override def topicError: String = "JuicioObnUpdatedFronDto_error"

  override def processInput(input: String): Either[Throwable, JuicioObnUpdatedFromDto] = {
    decode[JuicioObnUpdatedFromDto](input)
  }

  override def processMessage(registro: JuicioObnUpdatedFromDto): Future[Response.SuccessProcessing] = {


    val projection = JuicioObnUpdatedFromDtoProjection(registro)

    for {
      done <- r.cassandraWrite.writeState(projection).andThen {
        case Failure(exception) => log.error("Dont persist juicio_obn" + exception)
        case Success(value) => {
          log.debug("Persist juicio_obn" + value)
        }
      }

    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}

