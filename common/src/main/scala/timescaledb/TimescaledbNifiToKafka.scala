package timescaledb

import org.slf4j.LoggerFactory
import timescaledb.TimescaledbReadsideToCass.{connectToTimescaledb, log}

import java.sql.{Connection, DriverManager, SQLException, Statement}
import java.util.Properties
import scala.util.{Failure, Success, Try}

object TimescaledbNifiToKafka {
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val database_name1 = Try(System.getenv("NAME_TABLE1")).getOrElse("no")

  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)
  try {
    val e = System.getenv("USER_POSTGRES")
    val e1 = System.getenv("PASSWORD_POSTGRES")
    val e2 = System.getenv("STRING_CONEXION_TIMESCALEDB")
    val e3 = System.getenv("NAME_TABLE1")
    log.error("CUMBIASO USER_POSTGRES -> " + e)
    log.error("CUMBIASO PASSWORD_POSTGRES -> " + e1)
    log.error("CUMBIASO STRING_CONEXION_TIMESCALEDB -> " + e2)
    log.error("CUMBIASO NAME_TABLE2 -> " + e3)
  } catch {
    case e: Throwable => log.error("CUMBIASO TimescaledbNifiToKafka -> " + e)
  }
  var INSERT_NIFI_TO_KAFKA= s"INSERT INTO ${database_name1}" + "  (time,ev_id,bob_canal_origen,paso_1,time_paso_1,topico) VALUES " +
    " (now(), ?, ?, ?, now(), ?);";

  def  connectToTimescaledb(url: String): Option[Connection] = {
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
        log.error("Error connectToTimescaledb 2 " + e)
        Thread.sleep(5000)
        connectToTimescaledb(url)
    }
  }

  def connOracleNifi(input: String, topico: String) = {
    if (enable.equals("true")) {
      updateData(input, topico, "ev_id", "paso_1", "time_paso_1")
    }

  }
  val connection = connectToTimescaledb(url)
  @throws[SQLException]
  def updateData(input: String, topico: String, ev_id: String, paso: String, time_paso: String): Unit = {
    val a: List[String] = input match {
      case i if i.equals("input") => List("None")
      case _ => {
        val trimmedList: List[String] = input.split("\"").map(_.trim).toList
        val ev_id = trimmedList(3)
        val a = trimmedList.indexOf("BOB_CANAL_ORIGEN")
        val bob_canal_origenA = trimmedList(a + 1) match {
          case s if s.equals(": null,") => "TAX"
          case _ if a.equals(-1) => "TAX"
          case _ => trimmedList(a + 2)
        }
        val l: List[String] = List(a.toString, ev_id.toString, bob_canal_origenA.toString)
        l
      }
    }
    val conn = connectToTimescaledb(url)
    try {
      //log.error("CUMBIA  try ")
      if (!connection.getOrElse("no").equals("no")) {
        //log.error("CUMBIA  if ")
        val stmt = connection.get.prepareStatement(INSERT_NIFI_TO_KAFKA)
        //stmt.get.set(1, ZonedDateTime.now(ZoneId.of("UTC-3")))

        //log.error("CUMBIA  setString(1, ev_id) ")
        stmt.setString(1, a(1))
        stmt.setString(2, a(2))
        stmt.setString(3, paso)
        stmt.setString(4, topico)
        //log.error("CUMBIA  setString(2, paso) ")
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
        log.error("Error connOracleNifi -> " + e + " - id" + ev_id)
    }

      }



}