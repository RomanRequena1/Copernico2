package timescaledb

import akka.actor.Actor
import timescaledb.Timescaledb.{connOracleKafkaToWriteside, connOracleNifi, connOracleReadsideToCass, connOracleWriteSideToKafka}
import timescaledb.TimescaledbConnection.connect

import java.sql.Connection
case object Connec
case object Insert


case class Insert(input: String, topico: String)
case class InsertFromActor(ev_id: String)
case class InsertFromReadside(ev_id: String)
case class Insert2(ev_id: String)
class TimesActor extends Actor {
  var state = StateConnect()
  println("CUMBIA receive ")
  def receive = {

    case Connec =>
      println("CUMBIA receive 2 ")
      val c = connect(Reconnet(false, None))
      val t = c

      println("CUMBIA receive " + c.conn.get.getClientInfo)
      state.copy(conn = t.conn)

    case Insert(input: String, topico: String) =>
      connOracleNifi(input, topico, state.conn.get)
    case Insert2(ev_id) =>
      connOracleKafkaToWriteside(ev_id, state.conn.get)
    case InsertFromActor(ev_id) =>
      connOracleWriteSideToKafka(ev_id, state.conn.get)
    case InsertFromReadside(ev_id) =>
      connOracleReadsideToCass(ev_id, state.conn.get)
  }
}







