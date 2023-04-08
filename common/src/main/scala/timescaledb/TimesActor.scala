package timescaledb

import akka.actor.{Actor, ActorRef, ActorSelection, scala2ActorRef}
import org.slf4j.LoggerFactory
import timescaledb.Timescaledb.{connOracleKafkaToWriteside, connOracleNifi, connOracleReadsideToCass, connOracleWriteSideToKafka}
import timescaledb.TimescaledbConnection.{connect, log}

import java.sql.{Connection, SQLException}
import scala.concurrent.ExecutionContext.Implicits.global
case object Connec
case object Insert

case class Insert(input: String, topico: String, actorRef: ActorRef)
case class InsertFromActor(ev_id: String, actorRef: ActorSelection)
case class InsertFromReadside(ev_id: String, actorRef: ActorSelection)
case class Insert2(ev_id: String, actorRef: ActorRef)
class TimesActor extends Actor {
  private val log = LoggerFactory.getLogger(this.getClass)

  var state: Option[Connection] = None
  var stateInsert: String = "off"
  println("CUMBIA receive ")
  def receive = {

    case Connec =>
      stateInsert = "off"
      println("CUMBIA receive 2 ")
      println("CUMBIA receive 3" + state.getOrElse("None"))
      val c =  connect(0)
      println("CUMBIA + " + c)
      println("CUMBIA ++ " + Some(c))
      state = Some(c)
      stateInsert = "on"
      println("CUMBIA receive 3.5 " + state.getOrElse("None"))



      //println("CUMBIA receive 3" + c.conn.get.getClientInfo)
      //state.copy(conn = c.conn)

    case Insert(input: String, topico: String, actorRef: ActorRef) =>
      stateInsert match {
        case x if x.equals("on") => {
          println("CUMBIA receive Insert 2 ")
          connOracleNifi(input, topico, state.get, actorRef)
        }
      }
    case Insert2(ev_id, actorRef: ActorRef) =>
      stateInsert match {
        case x if x.equals("on") => {
          connOracleKafkaToWriteside(ev_id, state.get, actorRef)
          println("CUMBIA receive Insert2 2 ")
        }
      }

    case InsertFromActor(ev_id, actorRef: ActorSelection) =>
      stateInsert match {
        case x if x.equals("on") => {
          println("CUMBIA receive InsertFromActor 2 ")
          connOracleWriteSideToKafka(ev_id, state.get, actorRef)
        }
      }

    case InsertFromReadside(ev_id, actorRef: ActorSelection) =>
      stateInsert match {
        case x if x.equals("on") => {
          println("CUMBIA receive InsertFromReadside 2 ")
          connOracleReadsideToCass(ev_id, state.get, actorRef)
        }
      }

  }
}







