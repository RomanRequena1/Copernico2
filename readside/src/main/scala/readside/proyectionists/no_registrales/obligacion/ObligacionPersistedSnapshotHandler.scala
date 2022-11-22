package readside.proyectionists.no_registrales.obligacion

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import oracle.Oracle.{connOracleReadsideToCass}
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ObligacionPersistedSnapshotHandler(
                                          implicit
                                          r: MonitoringAndCassandraWrite
                                        ) extends ActorTransaction[ObligacionPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "ObligacionPersistedSnapshot"

  override def topicRetry: String = "ObligacionPersistedSnapshot_retry"

  override def topicError: String = "ObligacionPersistedSnapshot_error"

  import consumers.no_registral.obligacion.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObligacionPersistedSnapshot] =
    serialization
      .maybeDecode[ObligacionPersistedSnapshot](input)

  override def processMessage(registro: ObligacionPersistedSnapshot): Future[Response.SuccessProcessing] = {

    //recordLag(calculateLag(registro.deliveryId.toString))
    log.error("operacion: " + registro.operacion)

    val projection = ObligacionSnapshotProjection(registro)
    log.error("operacio 1: " + registro.operacion)

    for {
      done <- r.cassandraWrite.writeState(projection).andThen {
        case Failure(exception) => log.error("Dont persist obligacion" + exception )
        case Success(value) => log.error("Persist obligacion" + value )
          connOracleReadsideToCass(registro.deliveryId.toString(),"obligacion", registro.registro.get.BOB_CANAL_ORIGEN.getOrElse("TAX") )
      }

    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)








  }
}