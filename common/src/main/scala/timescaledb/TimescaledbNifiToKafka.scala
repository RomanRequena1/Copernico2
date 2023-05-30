package timescaledb

import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager, SQLException, Statement}
import java.util.Properties

import scala.util.{Failure, Success, Try}

object TimescaledbNifiToKafka {
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val database_name1 = Try(System.getenv("NAME_TABLE1")).getOrElse("no")
  val database_name2 = Try(System.getenv("NAME_TABLE2")).getOrElse("no")
  val database_name3 = Try(System.getenv("NAME_TABLE3")).getOrElse("no")
  val database_name4 = Try(System.getenv("NAME_TABLE4")).getOrElse("no")
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)

  def  connectToTimescaledb(url: String): Option[Connection] = {
    try {
      val props = new Properties()
      props.setProperty("connectTimeout", "0")
      props.setProperty("socketTimeout", "0")
      props.setProperty("user", user)
      props.setProperty("password", password)
      val conn = DriverManager.getConnection(url, props)
      Some(conn)
    } catch {
      case e: SQLException =>
        log.error("Error connectToTimescaledb 3 " + e)
        None
    }
  }

  def connOracleNifi(input: String, topico: String) = {
    if (enable.equals("true")) {
      updateData(input, topico, "ev_id", "paso_1", "time_paso_1")
    }

  }

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
      if (!conn.getOrElse("no").equals("no")) {
        conn.get.createStatement().executeUpdate(
          s"""
                            INSERT INTO $database_name1 (time,ev_id,bob_canal_origen,paso_1,time_paso_1,topico)
                            VALUES (
                                now(),
                                '${a(1)}',
                                '${a(2)}',
                                'PASO_1',
                                now(),
                                '${topico}'
                            )
                            """
        )
        conn.get.close()
      }
    }
    catch {
      case e: SQLException =>
        log.error("Error connOracleNifi -> " + e + " - id" + ev_id)
    }
      }



}