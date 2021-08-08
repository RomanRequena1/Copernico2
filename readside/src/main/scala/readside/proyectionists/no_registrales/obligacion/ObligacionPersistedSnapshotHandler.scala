package readside.proyectionists.no_registrales.obligacion
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection

import scala.concurrent.Future

class ObligacionPersistedSnapshotHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObligacionPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObligacionPersistedSnapshot"

  import consumers.no_registral.obligacion.infrastructure.json._


  override def processInput(input: String): Either[Throwable, ObligacionPersistedSnapshot] =
    serialization
      .maybeDecode[ObligacionPersistedSnapshot](input)

  override def processMessage(registro: ObligacionPersistedSnapshot): Future[Response.SuccessProcessing] = {
    val projection = ObligacionSnapshotProjection(registro)
    for {
      done <- r.cassandraWrite writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
