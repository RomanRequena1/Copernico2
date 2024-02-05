package readside.proyectionists.no_registrales.transferencia

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent.ObjetoVinculoSnapshotPersisted
import consumers.no_registral.tranferencia.infrastructure.json.ObjetoVinculoImplicits._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser.decode
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.transferencia.projections.TransferenciaSnapshotPersistedProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TransferenciaSnapshotPersistedHandler (
  implicit
  r: MonitoringAndCassandraWrite
  ) extends ActorTransaction[ObjetoVinculoSnapshotPersisted](r.monitoring)(r.actorTransactionRequirements) {
    @JsonIgnore
    private val log = LoggerFactory.getLogger(this.getClass)

    override def topic: String = "ObjetoVinculoPersisted"
    override def topicRetry: String =  "ObjetoVinculoPersisted_retry"
    override def topicError: String = "ObjetoVinculoPersisted_error"

    override def processInput(input: String): Either[Throwable, ObjetoVinculoSnapshotPersisted] = {
      decode[ObjetoVinculoSnapshotPersisted](input)
    }

    val cassandra = new CassandraWriteProduction()

    override def processMessage(registro: ObjetoVinculoSnapshotPersisted): Future[Response.SuccessProcessing] = {
      //recordLag(calculateLag(registro.deliveryId.toString))

      val projection: TransferenciaSnapshotPersistedProjection = TransferenciaSnapshotPersistedProjection(registro)
      for {
        done <- r.cassandraWrite.writeState(projection).andThen {
          case Failure(exception) => log.error("Dont persist sujeto " + exception )
          case Success(value) => log.debug("Persist sujeto " + value )
          //connOracleReadsideToCass(registro.deliveryId.toString(),"sujeto", registro.registro.get.SUJ_CANAL_ORIGEN.getOrElse("TAX"))
        }
      } yield SuccessProcessing(registro.aggregateRoot, 0)
    }

}
