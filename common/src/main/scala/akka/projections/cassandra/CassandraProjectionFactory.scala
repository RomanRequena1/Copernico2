package akka.projections.cassandra
import akka.actor.typed.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.projection.ProjectionId
import akka.projection.cassandra.scaladsl.{AtLeastOnceCassandraProjection, CassandraProjection}
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projections.ProjectionHandler

import scala.reflect.ClassTag

private[cassandra] object CassandraProjectionFactory {
  def createProjectionFor[T](
      system: ActorSystem[_],
      shardingNumber: Int,
      projectionId: String,
      projectionHandler: ProjectionHandler[T]
  ): AtLeastOnceCassandraProjection[EventEnvelope[T]] = {
    val tag = s"${projectionHandler.settings.tag}"
    println(s"CHECK factory ${system.name} - $tag - ${projectionHandler.getClass.toString}")
    val sourceProvider =
      EventSourcedProvider
      .eventsByTag[T](system = system, readJournalPluginId = CassandraReadJournal.Identifier, tag = tag)

    CassandraProjection.atLeastOnce(projectionId = ProjectionId(projectionId, tag),
                                    sourceProvider,
                                    handler = projectionHandler)
  }
}
