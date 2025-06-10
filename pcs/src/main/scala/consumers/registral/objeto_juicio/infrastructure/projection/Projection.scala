package consumers.registral.objeto_juicio.infrastructure.projection

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.Offset
import akka.projection.ProjectionBehavior
import akka.projection.ProjectionId
import akka.projection.cassandra.scaladsl.CassandraProjection
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.scaladsl.SourceProvider
import akka.projections.ProjectionSettings
import akka.stream.alpakka.cassandra.scaladsl.CassandraSessionRegistry

class Projection(system: ActorSystem[_], settings: ProjectionSettings) {
//  def sourceProvider(tag: String): SourceProvider[Offset, EventEnvelope[SujetoMockEvents]] =
//    EventSourcedProvider
//      .eventsByTag[SujetoMockEvents](
//        system,
//        readJournalPluginId = CassandraReadJournal.Identifier,
//        tag = tag)
//
//  def projection(tag: String) =
//    CassandraProjection.atLeastOnce(
//      projectionId = ProjectionId("shopping-carts", tag),
//      sourceProvider(tag),
//      handler = () => new SujetoMockProjectionHandler(settings, system))
//
//  ShardedDaemonProcess(system).init[ProjectionBehavior.Command](
//    name = "shopping-carts",
//    numberOfInstances = 1,
//    behaviorFactory = (i: Int) => ProjectionBehavior(projection(settings.tag)),
//    stopMessage = ProjectionBehavior.Stop)
}
