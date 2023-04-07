package timescaledb

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.{Connection, SQLException}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object TimescaledbConnection {

  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")

  private val log = LoggerFactory.getLogger(this.getClass)


  val config:HikariConfig = new HikariConfig();
  config.setJdbcUrl("jdbc:postgresql://172.22.2.1:5432/postgres");
  config.setUsername("postgres");
  config.setPassword("password");
  config.addDataSourceProperty("cachePrepStmts", "true");
  config.addDataSourceProperty("prepStmtCacheSize", "250");
  config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

  //val ds: HikariDataSource = new HikariDataSource(config);


  def connect(count1: Int): Connection = {

    println("CUMBIA count1 " + count1)
    var count = count1

    try {
      println("CUMBIA connect post Thread ")
      val c: Connection = new HikariDataSource(config).getConnection()
      println("CUMBIA connect true")
      c
    }
    catch {
      case ex:Throwable=>
        count += 1
        println("CUMBIA connect false")
        println("CUMBIA open connection to timescaledb 2" + ex.getMessage + " - " + ex.getCause)
        connect(count)

    }
    //var con: Option[Connection] = None
    /*val v = Future(new HikariDataSource(config).getConnection())
    Thread.sleep(10000)
    v.andThen {
      case Success(value) =>
        println("CUMBIA connect true")
        value
      case Failure(exception) =>

        count += 1
        println("CUMBIA connect false" )
        log.error("ERROR CUMBIA open connection to timescaledb 2" + exception.getMessage + " - " + exception.getCause)
        connect(count)
    }*/


  }







    /*println("count")
    var count = count1
    var r = ""
    var t: Option[Connection] = f.conn
    Future(new HikariDataSource(config).getConnection()).onComplete {
      case Failure(exception) => {
        println("CUMBIA connect false" )
        log.error("ERROR CUMBIA open connection to timescaledb 2" + exception.getMessage + " - " + exception.getCause)
        r = "No"
      }
      case Success(value) =>
        println("CUMBIA connect true")
        r = "Si"
        t = Some(value)
        println(" t -> " + t)
    }
    Thread.sleep(10000)
    if (r.equals("Si")){

      println(" t 1 -> " + t)
      Reconnet(true, t)
    }
    else {
      count += 1
      connect(Reconnet(false, None), count)

    }*/
    /*f match {

    case x if x.status.equals(true)   =>
      println("CUMBIA connect true")
      x
    case x if x.status.equals(false) => {
      println("CUMBIA connect Thread ")
      //Thread.sleep(10000)


      try {
        println("CUMBIA connect post Thread ")
        val c: Connection = new HikariDataSource(config).getConnection()
        println("CUMBIA connect true" )
        connect(Reconnet(true, Some(c)))
      }
      catch {
        case ex: SQLException =>
          println("CUMBIA connect false" )
          log.error("ERROR open connection to timescaledb 2" + ex.getMessage + " - " + ex.getCause)
          connect(Reconnet(false, None))

      }


    }
  }*/



}
