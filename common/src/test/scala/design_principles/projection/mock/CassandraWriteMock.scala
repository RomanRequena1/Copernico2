package design_principles.projection.mock

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import akka.{Done, actor}
import cassandra.write.CassandraWrite
import design_principles.actor_model.Event
import org.slf4j.LoggerFactory
import cassandra.CqlSessionSingleton
import com.datastax.oss.driver.api.core.cql.{AsyncResultSet, Row}

import scala.jdk.FutureConverters.CompletionStageOps
import scala.util.{Failure, Success}


class CassandraWriteMock()
    extends CassandraWrite {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val session = CqlSessionSingleton.session

  override def writeState[E <: Event](
      state: ddd.ReadSideProjection[E]
  )(implicit ec: ExecutionContext): Future[Done] = {
    val result = for {
      done <- state.updateReadside()
    } yield done
    result.onComplete {
      case Failure(throwable) =>
        logger.warn("Cassandra failed with {} due to {}", state.event.toString, throwable.toString)
      case Success(value) =>
        logger.debug("Cassandra succeeded with value {}", value.toString)
    }
    result
  }

  override def cql(cql: String)(implicit ec: ExecutionContext): Future[Done] = {
    session.executeAsync(cql).asScala.map { resultado =>
      akka.Done
    }
  }

  override def cqlSelect(cql: String)(implicit ec: ExecutionContext): Future[AsyncResultSet] = {
    session.executeAsync(cql).asScala
  }
}
