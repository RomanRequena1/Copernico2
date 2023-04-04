package timescaledb
import org.slf4j.LoggerFactory

import java.sql.Connection
import scala.util.Try

object Timescaledb {
  val database_name1 = Try(System.getenv("NAME_TABLE1")).getOrElse("no")
  val database_name2 = Try(System.getenv("NAME_TABLE2")).getOrElse("no")
  val database_name3 = Try(System.getenv("NAME_TABLE3")).getOrElse("no")
  val database_name4 = Try(System.getenv("NAME_TABLE4")).getOrElse("no")
  private val log = LoggerFactory.getLogger(this.getClass)

  def connOracleNifi(input: String, topico: String, conn: Connection) = {
    log.error("CUMBIA llego al test connOracleNifiT")

    updateData(input, topico, "ev_id", "paso_1", "time_paso_1", conn)
  }

  def connOracleKafkaToWriteside(ev_id: String, conn: Connection) = {
    log.error("CUMBIA llego al test connOracleKafkaToWritesideT")
    updateData2("input", "topico", ev_id, "paso_2", "time_paso_2", conn)
  }

  def connOracleWriteSideToKafka(ev_id: String, conn: Connection) = {
    log.error("CUMBIA llego al test connOracleWriteSideToKafka")
    insertFromActor("input", "topico", ev_id, "paso_3", "time_paso_3", conn)
  }

  def connOracleReadsideToCass(ev_id: String, conn: Connection) = {
    log.error("CUMBIA llego al test connOracleReadsideToCass")
    insertFromReadside("input", "topico", ev_id, "paso_4", "time_paso_4", conn)
  }


  def updateData(input: String, topico: String, ev_id: String, paso: String, time_paso: String, conn: Connection): Unit = {

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

  def updateData2(input: String, topico: String, ev_id: String, paso: String, time_paso: String, conn: Connection): Unit = {


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
  def insertFromActor(input: String, topico: String, ev_id: String, paso: String, time_paso: String, conn: Connection): Unit = {


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

  def insertFromReadside(input: String, topico: String, ev_id: String, paso: String, time_paso: String, conn: Connection): Unit = {


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