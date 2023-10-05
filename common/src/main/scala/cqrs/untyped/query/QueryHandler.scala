package cqrs.untyped.query

import com.fasterxml.jackson.annotation.JsonIgnore
import design_principles.actor_model.Query
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.util.Try

trait QueryHandler[P[_], Q <: Query] {
  def handle(query: Q): P[Q#ReturnType]
  @JsonIgnore
  val log: Logger = LoggerFactory.getLogger(this.getClass)

}

object QueryHandler {
  trait AsyncQueryHandler[Q <: Query] extends QueryHandler[Future, Q]
  trait SyncQueryHandler[Q <: Query] extends QueryHandler[Try, Q]
}
