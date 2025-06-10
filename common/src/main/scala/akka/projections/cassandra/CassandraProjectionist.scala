package akka.projections.cassandra

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.projection.ProjectionBehavior
import akka.projections.{ProjectionHandler, ProjectionSettings}

object CassandraProjectionist {

  case class CassandraProjectionistRequirements[T](
      projectionSettings: ProjectionSettings,
      system: ActorSystem[_],
      projectionHandler: ProjectionHandler[T]
  )
  def startProjection[T](requirements: CassandraProjectionistRequirements[T]): Unit = {
    val system = requirements.system
    val projectionSettings = requirements.projectionSettings
    val projectionHandler = requirements.projectionHandler
    val shardingSettings = ClusterShardingSettings(system)
    val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)

    println("CHECK iniciar pryec")

    println(s"projectionSettings.name: ${projectionSettings.name}")
    println(s"projectionSettings.parallelism: ${projectionSettings.parallelism}")
    println(s"shardedDaemonProcessSettings: ${shardedDaemonProcessSettings.toString}")
    println(s"system: ${system.name}")
    println(s"projectionSettings.projectionId: ${projectionSettings.projectionId}")
    println(s"projectionHandler: ${projectionHandler.getClass.toString}")

    ShardedDaemonProcess(system).init(
      projectionSettings.name,
      projectionSettings.parallelism,
      (shardingId: Int) =>
        ProjectionBehavior(
          CassandraProjectionFactory.createProjectionFor(
            system,
            shardingId,
            projectionSettings.projectionId,
            projectionHandler
          )
        ),
      shardedDaemonProcessSettings,
      Some(ProjectionBehavior.Stop)
    )
  }
}
