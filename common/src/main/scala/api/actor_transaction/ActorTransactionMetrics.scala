package api.actor_transaction

import akka.pattern.AskTimeoutException
import design_principles.actor_model.Response
import monitoring.{Counter, Histogram, Monitoring}
import org.slf4j.LoggerFactory
import serialization.SerializationError

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

abstract class ActorTransactionMetrics(
    monitoring: Monitoring
)(implicit ec: ExecutionContext) {

  final private val metricPrefix = "actor-transaction"
  final private val controllerId = api.Utils.Transformation.to_underscore(this.getClass.getSimpleName)
  final protected val requests: Counter = monitoring.counter(s"$metricPrefix-$controllerId-request")
  final protected val errors: Counter = monitoring.counter(s"$metricPrefix-$controllerId-error")
  final protected val latency: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-latency")
  final protected val lag: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-lag")

  private final val log = LoggerFactory.getLogger(this.getClass)

  final protected def recordRequests(): Unit =
    requests.increment()

  final protected def recordLag(n: Long): Unit =
    lag.record(n)

  final protected def recordLatency(future: Future[Response.SuccessProcessing]): Unit =
    latency.recordFuture(future)
  final protected def recordErrors(throwable: Throwable): Unit =
    throwable match {
      case e: SerializationError =>
        errors.increment()
        log.error(e.getMessage)
      case e: AskTimeoutException =>
        errors.increment()
        log.error(e.getMessage)
      case unexpectedException: Throwable =>
        errors.increment()
        log.error(unexpectedException.getMessage)
    }

  private def toLocalDateTime(num: String) = {
     val fechaString =
       s"${num(0)}${num(1)}${num(2)}${num(3)}-${num(4)}${num(5)}-${num(6)}${num(7)}T${num(8)}${num(9)}:${num(10)}${
         num(
           11
         )
       }:${num(12)}${num(13)}.${num(14)}${num(15)}${num(16)}"
     val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
     LocalDateTime.parse(fechaString, formatter)
  }

  protected def calculateLag(evId: String) = {
     //time in the source
     //GMT -3
     val ti: LocalDateTime = toLocalDateTime(evId)
     //time in the sink
     //GMT -3
     val tf: LocalDateTime = ZonedDateTime.now(ZoneId.of("UTC-3")).toLocalDateTime
     //difference
     ChronoUnit.MILLIS.between(ti, tf)
  }

}
