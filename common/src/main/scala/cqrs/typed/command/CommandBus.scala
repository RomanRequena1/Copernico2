package cqrs.typed.command

import akka.actor.Status.Success
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import design_principles.actor_model.{Command, Response}

import scala.reflect.ClassTag

trait CommandBus[Event, State] {

  type AkkaEffect = Effect[Event, State]

  def publish[C <: Command, Response](state: State, command: C)(replyTo: ActorRef[Success]): AkkaEffect

  def subscribe[C <: Command: ClassTag](handler: C => State => ActorRef[Success] => AkkaEffect): Unit

}
