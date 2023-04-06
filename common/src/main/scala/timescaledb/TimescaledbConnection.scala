package timescaledb

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global

import java.sql.{Connection, SQLException}
import scala.concurrent.Future
import scala.util.Try

object TimescaledbConnection {

  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")

  private val log = LoggerFactory.getLogger(this.getClass)


  val config:HikariConfig = new HikariConfig();
  config.setJdbcUrl("jdbc:postgresql://172.22.0.24:5432/postgres");
  config.setUsername("postgres");
  config.setPassword("password");
  config.addDataSourceProperty("cachePrepStmts", "true");
  config.addDataSourceProperty("prepStmtCacheSize", "250");
  config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

  val ds: HikariDataSource = new HikariDataSource(config);


  def connect(f:Reconnet) : Reconnet =

    f match {

    case x if x.status.equals(true)   =>
      println("CUMBIA connect true" + x.conn.get.getClientInfo)
      x
    case x if x.status.equals(false) => {
      //println("CUMBIA connect false" + c.value.get.get.getClientInfo)
      Thread.sleep(10000)
      lazy val c: Future[Connection] = Future(ds.getConnection())

      try {
        c
        connect(Reconnet(true, Some(c.value.get.get)))
      }
      catch {
        case ex: SQLException =>
          log.error("ERROR open connection to timescaledb" + ex.getMessage + " - " + ex.getCause)
          connect(Reconnet(false, Some(c.value.get.get)))

      }


    }
  }




}
