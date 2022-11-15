package oracle
import java.sql.Connection
import _root_.oracle.jdbc.pool.OracleDataSource
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

object oracle {
  private val config: Config = ConfigFactory.load()
  val ip = config.getString("oracle.ip")
  val port = config.getString("oracle.port")
  private val log = LoggerFactory.getLogger(this.getClass)
  def connOracleKafkaToWriteside(ev_id: String, entidad: String, bob_canal_origen: String) = {

    val queryObligacion = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '05'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
    val queryObjeto = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '05'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
    val querySujeto = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '05'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
    val oracleUser = "usrnifi"
    val oraclePassword = "usrnifi"
    val oracleURL = s"jdbc:oracle:thin:@${ip}:${port}/PTAXDESA"
    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)
    val con = ods.getConnection()
    val statement = con.createStatement()
    statement.setFetchSize(1000)      // important

    entidad match {
      case x if x == "obligacion" => statement.executeUpdate(queryObligacion)
      case x if x == "objeto" => statement.executeUpdate(queryObjeto)
      case x if x == "sujeto" => statement.executeUpdate(querySujeto)
      case _ => log.error("Dont exist this entity")
    }
  }

  def connOracleReadsideToCass(ev_id: String,  entidad: String, bob_canal_origen:String) = {

    val queryObligacion = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
    val queryObjeto = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """
    val querySujeto = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06'
           where EV_ID = '${ev_id}' and BOB_CANAL_ORIGEN = '${bob_canal_origen}'
    """

    val oracleUser = "usrnifi"
    val oraclePassword = "usrnifi"
    val oracleURL = s"jdbc:oracle:thin:@${ip}:${port}/PTAXDESA"
    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)
    val con = ods.getConnection()
    val statement = con.createStatement()
    statement.setFetchSize(1000)  // important

    entidad match {
      case x if x == "obligacion" => statement.executeUpdate(queryObligacion)
      case x if x == "objeto" => statement.executeUpdate(queryObjeto)
      case x if x == "sujeto" => statement.executeUpdate(querySujeto)
      case _ => log.error("Dont exist this entity")
    }

  }

}

