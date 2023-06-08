package timescaledb

import org.slf4j.LoggerFactory
import timescaledb.TimescaledbPcsToKafka.database_name3

import java.sql.{Connection, DriverManager, SQLException, Statement, Timestamp}
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object TimescaledbReadsideToCass {
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val database_name4 = Try(System.getenv("NAME_TABLE4")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)
  log.error("CUMBIASO USER_POSTGRES -> " + user)
  log.error("CUMBIASO PASSWORD_POSTGRES -> " + password)
  log.error("CUMBIASO STRING_CONEXION_TIMESCALEDB -> " + url)
  log.error("CUMBIASO NAME_TABLE4 -> " + database_name4)
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")

  try {
    System.getenv("USER_POSTGRES")
    System.getenv("PASSWORD_POSTGRES")
    System.getenv("STRING_CONEXION_TIMESCALEDB")
    System.getenv("NAME_TABLE2")

  } catch {
    case e: Exception => log.error("CUMBIASO TimescaledbKafkaToPcs-> " + e)
  }
  var INSERT_READSIDE_TO_CASS = s"INSERT INTO ${database_name4}" + "  (time,ev_id,paso_4,time_paso_4) VALUES " +
    " (now(), ?, ?, ?);";
  def connectToTimescaledb(url: String): Option[Connection] = {
    try {
      //log.error("connectToTimescaledb")
      val props = new Properties()
      props.setProperty("connectTimeout", "0")
      props.setProperty("socketTimeout", "0")
      props.setProperty("user", user)
      props.setProperty("password", password)
      val conn = DriverManager.getConnection(url, props)
      Some(conn)
    } catch {
      case e: Exception =>
        log.error("Error connectToTimescaledb 4 " + e)
        Thread.sleep(5000)
        connectToTimescaledb(url)
    }
  }

  def connOracleReadsideToCass(ev_id: String) = {
    if (enable.equals("true")) {
      updateData(ev_id, "paso_4")
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
        val stmt = connection.get.prepareStatement(INSERT_READSIDE_TO_CASS)
        //stmt.get.set(1, ZonedDateTime.now(ZoneId.of("UTC-3")))
        stmt.setString(1, ev_id)
        //log.error("CUMBIA  setString(1, ev_id) ")
        stmt.setString(2, paso)
        //log.error("CUMBIA  setString(2, paso) ")
        stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()))
        //stmt.get.setTimestamp(4, ZonedDateTime.now(ZoneId.of("UTC-3")))
        stmt.addBatch()
        //log.error("CUMBIA  addBatch ")
        //log.error("CUMBIA  stmt.get " + stmt)
        stmt.executeBatch()
        //count = count + 1
        //log.error("CUMBIA  count + 1 "+  count)
      }

    }
    catch {
      case e: SQLException =>
        log.error("Error connOracleReadsideToCass -> " + e + " - id" + ev_id)
    }


  }

}
