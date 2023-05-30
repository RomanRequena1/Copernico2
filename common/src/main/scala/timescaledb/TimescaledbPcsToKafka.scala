package timescaledb

import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager, SQLException, Statement}
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object TimescaledbPcsToKafka {
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val database_name1 = Try(System.getenv("NAME_TABLE1")).getOrElse("no")
  val database_name2 = Try(System.getenv("NAME_TABLE2")).getOrElse("no")
  val database_name3 = Try(System.getenv("NAME_TABLE3")).getOrElse("no")
  val database_name4 = Try(System.getenv("NAME_TABLE4")).getOrElse("no")
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)

  def connectToTimescaledb(url: String): Option[Connection] = {
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

  def connOracleWriteSideToKafka(ev_id: String) = {
    if (enable.equals("true")) {
      updateData(ev_id, "paso_3")
    }
  }

  @throws[SQLException]
  def updateData(ev_id: String, paso: String): Unit = {

    val conn = connectToTimescaledb(url)



        try {
          if (!conn.getOrElse("no").equals("no")) {
            conn.get.createStatement()
              .executeUpdate(
                s"""
                          INSERT INTO $database_name3 (time,ev_id,paso_3,time_paso_3)
                          VALUES (
                              now(),
                              '${ev_id}',
                              '${paso}',
                              now()
                          )
                          """
              )
            conn.get.close()
          }
        }
        catch {
          case e: SQLException =>
            log.error("Error connOracleWriteSideToKafka -> " + e + " - id" + ev_id)
        }


  }

}
