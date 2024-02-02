package readside.proyectionists.no_registrales.transferencia

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import net.logstash.logback.argument.StructuredArguments.r
import org.slf4j.LoggerFactory

class TransferenciaSnapshotPersistedHandler (
  implicit
  r: MonitoringAndCassandraWrite
  ) extends ActorTransaction[TransferenciaSnapshotPersisted](r.monitoring)(r.actorTransactionRequirements) {
    @JsonIgnore
    private val log = LoggerFactory.getLogger(this.getClass)

    override def topic: String = "SujetoSnapshotPersisted"
    override def topicRetry: String =  "SujetoSnapshotPersisted_retry"
    override def topicError: String = "SujetoSnapshotPersisted_error"

    import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits._

    override def processInput(input: String): Either[Throwable, SujetoSnapshotPersisted] = {
      decode[SujetoSnapshotPersisted](input)
    }

    val cassandra = new CassandraWriteProduction()

    override def processMessage(registro: SujetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
      //recordLag(calculateLag(registro.deliveryId.toString))

      val projection = SujetoSnapshotPersistedProjection(registro)
      for {
        done <- r.cassandraWrite.writeState(projection).andThen {
          case Failure(exception) => log.error("Dont persist sujeto " + exception )
          case Success(value) => log.debug("Persist sujeto " + value )
          //connOracleReadsideToCass(registro.deliveryId.toString(),"sujeto", registro.registro.get.SUJ_CANAL_ORIGEN.getOrElse("TAX"))
        }
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }

}
