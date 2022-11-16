package oracle
import _root_.oracle.jdbc.pool.OracleDataSource
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

object oracle {
  private val config: Config = ConfigFactory.load()
  val url = config.getString("oracle.url")
  val user = config.getString("oracle.user")
  val password = config.getString("oracle.password")

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
    val oracleUser = s"${user}"
    val oraclePassword = s"${password}"
    val oracleURL = s"${url}"
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

    val oracleUser = s"${user}"
    val oraclePassword = s"${password}"
    val oracleURL = s"${url}"
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

