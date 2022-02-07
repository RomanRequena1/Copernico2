package readside.proyectionists.no_registrales.obligacion
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection

class ObligacionPersistedSnapshotHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObligacionPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObligacionPersistedSnapshot"
  override def topicRetry: String = "ObligacionPersistedSnapshot_retry"
  override def topicError: String = "ObligacionPersistedSnapshot_error"

  private val log = LoggerFactory.getLogger(this.getClass)

  import consumers.no_registral.obligacion.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObligacionPersistedSnapshot] =
    serialization
      .maybeDecode[ObligacionPersistedSnapshot](input)

  override def processMessage(registro: ObligacionPersistedSnapshot): Future[Response.SuccessProcessing] = {
    recordLag(calculateLag(registro.deliveryId.toString))
    if (registro.operacion.equals("U")) {
      val projection = ObligacionSnapshotProjection(registro)
      for {
        done <- r.cassandraWrite writeState projection
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    } else {
      val cassandra = new CassandraWriteProduction()
      for {
        done <- cassandra
          .cql(
            s"""
          DELETE FROM read_side.buc_obligaciones """ +
            """ WHERE bob_suj_identificador = """ +
            s""" '${registro.sujetoId}' """ +
            s""" and bob_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
            s""" and bob_soj_identificador = '${registro.objetoId}' """ +
            s""" and bob_obn_id = '${registro.obligacionId}'
          """
          )
          .recover { ex: Throwable =>
            log.error(ex.getMessage)
            ex
          }
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }
  }

}
