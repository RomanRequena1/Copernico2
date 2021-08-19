package readside.proyectionists.no_registrales.obligacion.projectionists

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.obligacion.domain.ObligacionEvents.{ObligacionAddedExencion, ObligacionPersistedSnapshot}
import consumers.no_registral.obligacion.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class ObligacionDeletedSnapshotHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObligacionPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObjetoSnapshotDeleted"

  override def processInput(input: String): Either[Throwable, ObligacionPersistedSnapshot] =
    serialization
      .maybeDecode[ObligacionPersistedSnapshot](input)

  val cassandra = new CassandraWriteProduction()
  private val log = LoggerFactory.getLogger(this.getClass)

  override def processMessage(registro: ObligacionPersistedSnapshot): Future[Response.SuccessProcessing] = {
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
