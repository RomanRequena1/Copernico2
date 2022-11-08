package oracle
import java.sql.Connection
import _root_.oracle.jdbc.pool.OracleDataSource
import org.slf4j.LoggerFactory
object oracle {


  private val log = LoggerFactory.getLogger(this.getClass)
  def connOracleKafkaToWriteside(sujetoId: String,tipoObjeto: String,objetoId: String,obligacionId: String) = {
    //UPDATE readside SET out='hola1' WHERE id = '123'
    val query = s"""
    UPDATE readside SET outtocass = 'true' WHERE sujetoId = '${sujetoId}' and tipoObjeto = '${tipoObjeto}' and objetoId = '${objetoId}' and obligacionId = '${obligacionId}' and tipo = 'KW'
    """

    val connection : Connection = null
    val oracleUser = "sys as sysdba"
    val oraclePassword = "1234"
    val oracleURL = "jdbc:oracle:thin:@172.22.2.2:1521:xe"

    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)

    val con = ods.getConnection()
    val statement = con.createStatement()

    statement.setFetchSize(1000)      // important

    statement.executeUpdate(query)

  }
  def connOracleWriteSideToKafka(sujetoId: String,tipoObjeto: String,objetoId: String,obligacionId: String) = {
    //UPDATE readside SET out='hola1' WHERE id = '123'
    val query = s"""
      UPDATE readside SET outtocass='true' WHERE sujetoId = '${sujetoId}' and tipoObjeto = '${tipoObjeto}' and objetoId = '${objetoId}' and obligacionId = '${obligacionId}' and tipo = 'WK'

    """

    val connection : Connection = null
    val oracleUser = "sys as sysdba"
    val oraclePassword = "1234"
    val oracleURL = "jdbc:oracle:thin:@172.22.2.2:1521:xe"

    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)

    val con = ods.getConnection()
    val statement = con.createStatement()

    statement.setFetchSize(1000)      // important

    statement.executeUpdate(query)


  }

  def connOracleReadsideToCass(sujetoId: String,tipoObjeto: String,objetoId: String,obligacionId: String) = {

    val query = s"""
  UPDATE readside SET outtocass='true' WHERE sujetoId = '${sujetoId}' and tipoObjeto = '${tipoObjeto}' and objetoId = '${objetoId}' and obligacionId = '${obligacionId}' and tipo = 'KR'
    """

    val connection : Connection = null
    val oracleUser = "sys as sysdba"
    val oraclePassword = "1234"
    val oracleURL = "jdbc:oracle:thin:@172.22.2.2:1521:xe"

    val ods = new OracleDataSource()
    ods.setUser(oracleUser)
    ods.setURL(oracleURL)
    ods.setPassword(oraclePassword)

    val con = ods.getConnection()
    val statement = con.createStatement()

    statement.setFetchSize(1000)      // important

    statement.executeUpdate(query)

  }
  //connOracleWriteSideToKafka("20-06411831-7","i","270618850898","201500098")
  //connOracleReadsideToCass("20-06411831-7","I","270618850898","201500098")




}
