package api.actor_transaction

import akka.pattern.AskTimeoutException
import com.datastax.oss.driver.api.core.DriverTimeoutException
import com.fasterxml.jackson.annotation.JsonIgnore
import ddd.ExternalDto
import design_principles.actor_model.Response
import monitoring.{Counter, Histogram, Monitoring}
import org.slf4j.LoggerFactory

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
  final protected val errorsATO: Counter = monitoring.counter(s"$metricPrefix-$controllerId-error-asktimeout")
  final protected val latency: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-latency")
  final protected val lag: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-lag")

  final protected val idempotency: Counter = monitoring.counter(s"$metricPrefix-$controllerId-idempotency")
  final protected val pagos: Counter = monitoring.counter(s"$metricPrefix-$controllerId-pagos")
  final protected val pagosError: Counter = monitoring.counter(s"$metricPrefix-$controllerId-pagos-error")
  final protected val idempotencyInt: Counter = monitoring.counter(s"$metricPrefix-$controllerId-idempotency-int")
  final protected val betterSorterEvents: Counter = monitoring.counter(s"$metricPrefix-$controllerId-better-sorter")


  @JsonIgnore
  private final val log = LoggerFactory.getLogger(this.getClass)

  final protected def recordRequests(): Unit =
    requests.increment()

  final protected def recordBetterSorter(): Unit =
    betterSorterEvents.increment()

  final protected def recordIdempotencyInternally(): Unit = {
    idempotencyInt.increment()
  }

  final protected def recordIdempotency(): Unit = {
    idempotency.increment()
  }

  final protected def recordPagos(): Unit = {
    pagos.increment()
  }

  final protected def recordLag(n: Long): Unit =
    lag.record(n)

  final protected def recordLatency(future: Future[Response.SuccessProcessing]): Unit =
    latency.recordFuture(future)

  private def extractRuleNumber(input: String): String = {
    Try {
      val pattern = """"RULE_NUMBER"\s*:\s*"([^"]*)"""".r
      pattern.findFirstMatchIn(input).map(_.group(1)).getOrElse("")
    }.getOrElse("")
  }

  private def extractErrorInfo(input: String): String = {
    Try {
      def getValue(key: String): String = {
        val pattern = s""""$key"\\s*:\\s*"([^"]*)"""".r
        pattern.findFirstMatchIn(input).map(_.group(1)).getOrElse("")
      }

      val evId = getValue("EV_ID")

      input match {
        case s if s.contains("BOB_OBN_ID") =>
          val sujIden = getValue("BOB_SUJ_IDENTIFICADOR")
          val sojTipo = getValue("BOB_SOJ_TIPO_OBJETO")
          val sojIden = getValue("BOB_SOJ_IDENTIFICADOR")
          val ruleNumber = extractRuleNumber(input)
//          s"[$evId - SUJ: $sujIden, SOJ_TIPO: $sojTipo, SOJ_ID: $sojIden, OBN_ID: $obnId, RULE: $ruleNumber]"
          s" [$evId - $sujIden, $sojIden-$sojTipo ($ruleNumber)]"


        case s if s.contains("SOJ_SUJ_IDENTIFICADOR") =>
          val sujIden = getValue("SOJ_SUJ_IDENTIFICADOR")
          val sojTipo = getValue("SOJ_TIPO_OBJETO")
          val sojIden = getValue("SOJ_IDENTIFICADOR")
          s" [$evId - $sujIden, $sojIden-$sojTipo]"

        case s if s.contains("SUJ_IDENTIFICADOR") =>
          val sujIden = getValue("SUJ_IDENTIFICADOR")
          s" [$evId - $sujIden]"

        case _ =>
          s" [$evId]"
      }
    }.getOrElse(" []")
  }

  final protected def recordErrors(throwable: Throwable, input: String): Unit = {

    throwable match {
      /*case e: SerializationError =>
        errors.increment()
        log.error(e.getMessage)*/
      case e: AskTimeoutException =>
        errors.increment()
        errorsATO.increment()
        log.error(e.getMessage + extractErrorInfo(input))
        if (extractRuleNumber(input).equals("-1") || extractRuleNumber(input).equals("-2")) {
          pagosError.increment()
        }
      case unexpectedException: Throwable =>
        errors.increment()
        log.error(unexpectedException.getMessage)
    }
  }

  private def toLocalDateTime(num: String) = {
     val fechaString =
       s"${num(0)}${num(1)}${num(2)}${num(3)}-${num(4)}${num(5)}-${num(6)}${num(7)}T${num(8)}${num(9)}:${num(10)}${
         num(
           11
         )
       }:${num(12)}${num(13)}.${num(14)}${num(15)}${num(16)}"
     val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
     //log.error("CUMBIA fechaString " + fechaString)
     //log.error("CUMBIA formatter " + formatter)
     //log.error("CUMBIA final " + LocalDateTime.parse(fechaString, formatter))

     LocalDateTime.parse(fechaString, formatter)
  }

  protected def calculateLag(evId: String): Long = {

     //time in the source
     //GMT -3
     val ti: LocalDateTime = toLocalDateTime(evId)
     //time in the sink
     //GMT -3
     val tf: LocalDateTime = ZonedDateTime.now(ZoneId.of("UTC-3")).toLocalDateTime
     //difference
     //log.error("CUMBIA evId " + evId)
     //log.error("CUMBIA ti " + ti)
     //log.error("CUMBIA tf " + tf)
     //log.error("CUMBIA tf - ti" + (ChronoUnit.MILLIS.between(ti, tf)))

     ChronoUnit.MILLIS.between(ti, tf)
  }

}
