package oracle
import _root_.oracle.jdbc.pool.OracleDataSource
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.util.Try

object Oracle {
  private val config: Config = ConfigFactory.load()
  val url = Try(System.getenv("STRING_CONEXION_ORACLE")).getOrElse("no")
  val user = Try(System.getenv("USER_ORACLE")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_ORACLE")).getOrElse("no")

  private val log = LoggerFactory.getLogger(this.getClass)

  val oracleUser = s"${user}"
  val oraclePassword = s"${password}"
  val oracleURL = s"${url}"
  val ods = new OracleDataSource()
  ods.setUser(oracleUser)
  ods.setURL(oracleURL)
  ods.setPassword(oraclePassword)


  def connOracleKafkaToWriteside(ev_id: String, entidad: String, bob_canal_origen: String) = {
    log.error("url" + url)
    log.error("user" + user)
    log.error("password" + password)
    try {

      val con = ods.getConnection()
      val statement = con.createStatement()
      val queryObligacion =
        s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '05'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
      val queryObjeto =
        s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '05'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
      val querySujeto =
        s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '05'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """

      statement.setFetchSize(1000) // important
      entidad match {
        case x if x == "obligacion" => statement.executeUpdate(queryObligacion)
        case x if x == "objeto" => statement.executeUpdate(queryObjeto)
        case x if x == "sujeto" => statement.executeUpdate(querySujeto)
        case _ => log.error("Dont exist this entity")
      }
    } catch {
      case e: Exception => log.error("Error connection to Oracle - " + e + " - [" + ev_id + "]")
    }
  }

  def connOracleReadsideToCass(ev_id: String,  entidad: String, bob_canal_origen:String) = {
    log.error("url" + url)
    log.error("user" + user)
    log.error("password" + password)
    try {
      val con = ods.getConnection()
      log.error("url" + url)
      log.error("user" + user)
      log.error("password" + password)
      val statement = con.createStatement()
      val queryObligacion =
        s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
      val queryObjeto =
        s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
      val querySujeto =
        s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
      statement.setFetchSize(1000) // important
      entidad match {
        case x if x == "obligacion" => statement.executeUpdate(queryObligacion)
        case x if x == "objeto" => statement.executeUpdate(queryObjeto)
        case x if x == "sujeto" => statement.executeUpdate(querySujeto)
        case _ => log.error("Dont exist this entity")
      }
    } catch {
      case e: Exception => log.error("Error connection to Oracle - " + e + " - [" + ev_id + "]")
    }
  }

}




