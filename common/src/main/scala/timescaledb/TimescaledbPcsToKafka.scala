package timescaledb

import org.slf4j.LoggerFactory
import timescaledb.TimescaledbReadsideToCass.{connectToTimescaledb, log}

import java.sql.{Connection, DriverManager, SQLException, Statement}
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object TimescaledbPcsToKafka {
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")

  val database_name3 = Try(System.getenv("NAME_TABLE3")).getOrElse("no")


  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)
  try {
    val e = System.getenv("USER_POSTGRES")
    val e1 = System.getenv("PASSWORD_POSTGRES")
    val e2 = System.getenv("STRING_CONEXION_TIMESCALEDB")
    val e3 = System.getenv("NAME_TABLE2")
    log.error("CUMBIASO USER_POSTGRES -> " + e)
    log.error("CUMBIASO PASSWORD_POSTGRES -> " + e1)
    log.error("CUMBIASO STRING_CONEXION_TIMESCALEDB -> " + e2)
    log.error("CUMBIASO NAME_TABLE3 -> " + e3)
  } catch {
    case e: Throwable => log.error("CUMBIASO TimescaledbPcsToKafka -> " + e)
  }
  var INSERT_PCS_TO_KAFKA = s"INSERT INTO ${database_name3}" + "  (time,ev_id,paso_3,time_paso_3) VALUES " +
    " (now(), ?, ?, now());";
  def connectToTimescaledb(url: String): Option[Connection] = {
    try {
      //log.error("connectToTimescaledb")
      val props = new Properties()
      props.setProperty("connectTimeout", "0")
      props.setProperty("socketTimeout", "0")
      props.setProperty("user", "copernico")
      props.setProperty("password", "c0p3rn1c0.303")
      val conn = DriverManager.getConnection("jdbc:postgresql://timescaledb-rentas.cba.gov.ar:5432/copernico", props)
      Some(conn)
    } catch {
      case e: Exception =>
        log.error("Error connectToTimescaledb 3 " + e)
        Thread.sleep(5000)
        connectToTimescaledb(url)
    }
  }

  def connOracleWriteSideToKafka(ev_id: String) = {
    if (enable.equals("true")) {
      updateData(ev_id, "paso_3")
    }
  }
  val connection = connectToTimescaledb(url)
  @throws[SQLException]
  def updateData(ev_id: String, paso: String): Unit = {

    //val conn = connectToTimescaledb(url)


    try {
      //log.error("CUMBIA  try ")
      if (!connection.getOrElse("no").equals("no")) {
        //log.error("CUMBIA  if ")
        val stmt = connection.get.prepareStatement(INSERT_PCS_TO_KAFKA)
        //stmt.get.set(1, ZonedDateTime.now(ZoneId.of("UTC-3")))
        stmt.setString(1, ev_id)
        //log.error("CUMBIA  setString(1, ev_id) ")
        stmt.setString(2, paso)
        //log.error("CUMBIA  setString(2, paso) ")
        //stmt.get.setTimestamp(4, ZonedDateTime.now(ZoneId.of("UTC-3")))
        stmt.addBatch()
        //log.error("CUMBIA  addBatch ")
        log.error("CUMBIA  stmt.get " + stmt)
        stmt.executeBatch()
        //count = count + 1
        //log.error("CUMBIA  count + 1 "+  count)
      }

    }
    catch {
      case e: SQLException =>
        log.error("Error connOracleKafkaToWriteside -> " + e + " - id" + ev_id)
    }


  }

}
