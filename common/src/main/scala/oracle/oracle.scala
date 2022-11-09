package oracle
import java.sql.Connection
import _root_.oracle.jdbc.pool.OracleDataSource
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat
import java.util.Calendar
object oracle {


  private val log = LoggerFactory.getLogger(this.getClass)
  def connOracleKafkaToWriteside(ev_id: String) = {

    val form = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    val c = Calendar.getInstance();



    val formattedDate = form.format(c.getTime());
    val query = s"""
            update tax.EVENTOS_OBN_LOGS
            set PASO = '05', fecha_03 = '${formattedDate}'
            where EV_ID = '${ev_id}'
    """
    log.debug("\nTime when pcs comes in " + formattedDate)
    val connection : Connection = null
    val oracleUser = "usrnifi"
    val oraclePassword = "usrnifi"
    val oracleURL = "jdbc:oracle:thin:@10.250.11.15:1521/PTAXDESA"

    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)

    val con = ods.getConnection()
    val statement = con.createStatement()

    statement.setFetchSize(1000)      // important

    statement.executeUpdate(query)

  }




  def connOracleReadsideToCass(ev_id: String) = {
    val form = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    val c = Calendar.getInstance();



    val formattedDate = form.format(c.getTime());
    val query = s"""
           update tax.EVENTOS_OBN_LOGS
           set PASO = '06', fecha_04 = '${formattedDate}'
           where EV_ID = '${ev_id}'
    """
    log.debug("Time when readside comes out " + formattedDate)
    val connection : Connection = null
    val oracleUser = "usrnifi"
    val oraclePassword = "usrnifi"
    val oracleURL = "jdbc:oracle:thin:@10.250.11.15:1521/PTAXDESA"

    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)

    val con = ods.getConnection()
    val statement = con.createStatement()

    statement.setFetchSize(1000)      // important

    statement.executeUpdate(query)

  }





}
