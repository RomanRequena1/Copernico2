package timescaledb

import org.slf4j.LoggerFactory

import java.sql.Connection
import java.util.Properties
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}



object Timescaledb2 {


  import java.sql.{DriverManager, SQLException}

  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")
  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val database_name1 = Try(System.getenv("NAME_TABLE1")).getOrElse("no")
  val database_name2 = Try(System.getenv("NAME_TABLE2")).getOrElse("no")
  val database_name3 = Try(System.getenv("NAME_TABLE3")).getOrElse("no")
  val database_name4 = Try(System.getenv("NAME_TABLE4")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)



  def connOracleNifi(input: String, topico: String) = {
    //log.error("CUMBIA llego al test connOracleNifiT")

    updateData(input,topico, "ev_id", "paso_1", "time_paso_1")
  }

  def connOracleKafkaToWriteside(ev_id: String) = {
    //log.error("CUMBIA llego al test connOracleKafkaToWritesideT")
    updateData("input","topico",ev_id,"paso_2", "time_paso_2")
  }
  def connOracleWriteSideToKafka(ev_id: String) = {
    //log.error("CUMBIA llego al test connOracleWriteSideToKafka")
    updateData("input","topico",ev_id, "paso_3", "time_paso_3")
  }
  def connOracleReadsideToCass(ev_id: String) = {
    //log.error("CUMBIA llego al test connOracleReadsideToCass")
    updateData("input","topico",ev_id, "paso_4", "time_paso_4")
  }

  def  connectToTimescaledb(url: String): Connection = {
    try {

      val props = new Properties()
      //props.setProperty("user", "fred")
      //props.setProperty("password", "secret")
      //props.setProperty("ssl", "true")
      props.setProperty("connectTimeout", "0")
      props.setProperty("socketTimeout", "0")
      props.setProperty("user", user)
      props.setProperty("password", password)
      val conn = DriverManager.getConnection(url, props)
      //System.out.println("Connected to the PostgreSQL server successfully.")
      conn
    } catch {
      case e: SQLException =>
        System.out.println(e.getMessage)
        connectToTimescaledb(url)
    }

    //val conn = Future(DriverManager.getConnection(url))

  }


  @throws[SQLException]
  def updateData(input: String, topico: String, ev_id: String,paso: String, time_paso: String): Unit = {


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
        val l: List[String] = List(a.toString,ev_id.toString,bob_canal_origenA.toString )
        l
      }

    }


    val conn = connectToTimescaledb(url)





    time_paso match {
      case tp if tp.equals("time_paso_1") => {
        val batch = conn.createStatement()

        batch.executeUpdate(
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

        conn.close()

      }

      case tp if tp.equals("time_paso_2") => {
        val batch2 = conn.createStatement()

        batch2.executeUpdate(
          s"""
                      INSERT INTO $database_name2 (time,ev_id,paso_2,time_paso_2)
                      VALUES (
                          now(),
                          '${ev_id}',
                          '${paso}',
                          now()
                      )
                      """
        )

        conn.close()

      }

      case tp if tp.equals("time_paso_3") => {
        val batch3 = conn.createStatement()

        batch3.executeUpdate(
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

        conn.close()
      }
      case tp if tp.equals("time_paso_4") => {
        val batch2 = conn.createStatement()

        batch2.executeUpdate(
          s"""
                      INSERT INTO $database_name4 (time,ev_id,paso_4,time_paso_4)
                      VALUES (
                          now(),
                          '${ev_id}',
                          '${paso}',
                          now()
                      )
                      """
        )

        conn.close()

      }
    }

  }
}