package timescaledb

import org.slf4j.LoggerFactory
import timescaledb.TimescaledbReadsideToCass.{connectToTimescaledb, log}

import java.sql.{Connection, DriverManager, PreparedStatement, SQLException, Statement, Timestamp}
import java.time.{ZoneId, ZonedDateTime}
import java.util.Properties
import scala.util.{Failure, Success, Try}

object TimescaledbKafkaToPcs {
  private val log = LoggerFactory.getLogger(this.getClass)
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val database_name2 = Try(System.getenv("NAME_TABLE2")).getOrElse("no")




  //val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")

  //var count = 0
  //var conn: Option[Connection] = None
  //var stmt: Option[PreparedStatement] = None
  var INSERT_NKAFKA_TO_PCS = s"INSERT INTO ${database_name2}" + "  (time,ev_id,paso_2,time_paso_2) VALUES " +
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
        log.error("Error connectToTimescaledb 1 " + e)
        Thread.sleep(300000)
        connectToTimescaledb(url)
    }
  }
  val connection = connectToTimescaledb(url)

  def connOracleKafkaToWriteside(ev_id: String) = {

      try {
        //log.error("CUMBIA  try ")
        if (!connection.getOrElse("no").equals("no")) {
          //log.error("CUMBIA  if ")
          val stmt = connection.get.prepareStatement(INSERT_NKAFKA_TO_PCS)
          //stmt.get.set(1, ZonedDateTime.now(ZoneId.of("UTC-3")))
          stmt.setString(1, ev_id)
          //log.error("CUMBIA  setString(1, ev_id) ")
          stmt.setString(2, "paso_2")
          stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()))
          //log.error("CUMBIA  setString(2, paso) ")
          //stmt.get.setTimestamp(4, ZonedDateTime.now(ZoneId.of("UTC-3")))
          stmt.addBatch()
          //log.error("CUMBIA  addBatch ")
          //log.error("CUMBIA  stmt.get "+  stmt)
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



  /*def updateData(ev_id: String, paso: String): Unit = {

    //log.error("CUMBIA Llego a updateData")
    //log.error("CUMBIA count: " + count)

      try {
        //log.error("CUMBIA  try ")
        if (!connection.getOrElse("no").equals("no")) {
          //log.error("CUMBIA  if ")
          val stmt = connection.get.prepareStatement(INSERT_NKAFKA_TO_PCS)
          //stmt.get.set(1, ZonedDateTime.now(ZoneId.of("UTC-3")))
          stmt.setString(1, ev_id)
          //log.error("CUMBIA  setString(1, ev_id) ")
          stmt.setString(2, paso)
          //log.error("CUMBIA  setString(2, paso) ")
          stmt.setTimestamp(4, ZonedDateTime.now(ZoneId.of("UTC-3")))
          stmt.addBatch()
          //log.error("CUMBIA  addBatch ")
          //log.error("CUMBIA  stmt.get "+  stmt)
          stmt.executeBatch()
          //count = count + 1
          //log.error("CUMBIA  count + 1 "+  count)
        }

      }
      catch {
        case e: SQLException =>
          log.error("Error connOracleKafkaToWriteside -> " + e + " - id" + ev_id)
      }
  }*/
}
