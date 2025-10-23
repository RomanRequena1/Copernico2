package readside.proyectionists.no_registrales.dmn

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.obligacion.domain.ObligacionEvents.DMNResumenPersisted
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser.decode
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.dmn.projectionists.DMNResumenSnapshotProjection
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import scala.concurrent.Future
import scala.util.{Failure, Success}

class DMNResumenPersistedSnapshotHandler(
                                  implicit
                                  r: MonitoringAndCassandraWrite
                                ) extends ActorTransaction[DMNResumenPersisted](r.monitoring)(r.actorTransactionRequirements) {

  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "DMNResumenPersistedSnapshot"

  override def topicRetry: String = "DMNResumenPersistedSnapshot_retry"

  override def topicError: String = "DMNResumenPersistedSnapshot_error"
  override def processInput(input: String): Either[Throwable, DMNResumenPersisted] =
    decode[DMNResumenPersisted](input)

  override def processMessage(registro: DMNResumenPersisted): Future[Response.SuccessProcessing] = {
    val projection = DMNResumenSnapshotProjection(registro)

    for {
      done <- r.cassandraWrite.writeState(projection).andThen {
        case Failure(exception) => {
          log.error("Dont persist DMNResumen" + exception)
        }
        case Success(value) => {
        }
      }
    } yield SuccessProcessing(registro.sujetoId, registro.deliveryId)
  }
}