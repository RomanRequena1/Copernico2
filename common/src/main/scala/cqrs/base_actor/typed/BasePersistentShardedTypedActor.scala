package cqrs.base_actor.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DispatcherSelector}
import akka.cluster.sharding.external.ExternalShardAllocationStrategy
import akka.cluster.sharding.typed.ClusterShardingSettings
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import design_principles.actor_model.mechanism.AbstractOverReplyTo.MessageWithAutomaticReplyTo
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

abstract class BasePersistentShardedTypedActor[
    ActorMessages <: design_principles.actor_model.ShardedMessage: ClassTag,
    ActorEvents,
    State <: BasePersistentShardedTypedActorAbstractState[ActorMessages, ActorEvents, State]
](
    state: State
)(implicit system: ActorSystem[_]) {

  val sharding: ClusterSharding = ClusterSharding(system)
  val TypeKey: EntityTypeKey[ActorMessages] = EntityTypeKey[ActorMessages](
    utils.Inference.getSimpleName(this.getClass.getName)
  )
  val NR_PARTITIONS: Int = Try(System.getenv("NR_PARTITIONS")).map(_.toInt).getOrElse(90)
  def getTags(event: ActorEvents): Set[String] = Set("")

  val shardActor: ActorRef[ActorMessages] = sharding.init(
    Entity(TypeKey) { context =>
      persistentEntity(context.entityId, context.shard)
    }.withAllocationStrategy(new ExternalShardAllocationStrategy(system, TypeKey.name))
      .withMessageExtractor(new LocalizedProcessingMessageExtractor[ActorMessages](NR_PARTITIONS))
      .withSettings(ClusterShardingSettings(system))
      .withEntityProps {
        // val dispatcherName = "my-dispatcher"
        val dispatcherName = utils.Inference.getSimpleName(this.getClass.getName)
        val dispatcher = Try {
          // when running the tests, the typed actors had troubles finding the config
          DispatcherSelector.default()
          //DispatcherSelector.fromConfig(dispatcherName)
        } match {
          case Failure(configurationException) => DispatcherSelector.default()
          case Success(dispatcher) => dispatcher
        }
        dispatcher
      }
  )

  def commandHandler(state: State, command: ActorMessages): Effect[ActorEvents, State]
  def eventHandler(state: State, event: ActorEvents): State

  def persistentEntity(entityId: String, shardedId: ActorRef[ClusterSharding.ShardCommand]): Behavior[ActorMessages] =
    Behaviors.setup { a =>
      println("CHECK Llego persis entity")
      EventSourcedBehavior[ActorMessages, ActorEvents, State](
        PersistenceId(TypeKey.name, entityId),
        emptyState = state,
        commandHandler = (state, message) => commandHandler(state, message),
        eventHandler = (state, evt) => eventHandler(state, evt)
      ).withTagger(event => getTags(event))
    }

  def getEntityRef(entityId: String): EntityRef[ActorMessages] =
    sharding.entityRefFor(TypeKey, entityId)

  def getEntityRefTyped[InMessage <: design_principles.actor_model.ShardedMessage, Response](
      entityId: String
  ): EntityRef[MessageWithAutomaticReplyTo[InMessage, Response]] = {
    val TypeKey = EntityTypeKey[MessageWithAutomaticReplyTo[InMessage, Response]](
      utils.Inference.getSimpleName(this.getClass.getName)
    )
    sharding.entityRefFor(TypeKey, entityId)
  }
}

object BasePersistentShardedTypedActor {}
