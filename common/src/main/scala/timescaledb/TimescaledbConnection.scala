package timescaledb

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import java.sql.{Connection, SQLException}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object TimescaledbConnection {

  val url = Try(System.getenv("STRING_CONEXION_TIMESCALEDB")).getOrElse("no")
  val user = Try(System.getenv("USER_POSTGRES")).getOrElse("no")
  val password = Try(System.getenv("PASSWORD_POSTGRES")).getOrElse("no")

  val enable_traz = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")

  private val log = LoggerFactory.getLogger(this.getClass)

  val config: HikariConfig = new HikariConfig();
  config.setJdbcUrl(url);
  config.setPoolName("Pool-test-trazabilidad")
  config.setUsername(user);
  config.setPassword(password);
  config.setMaximumPoolSize(20)
  config.setMaxLifetime(60000)
  config.setIdleTimeout(120000)
  config.setMinimumIdle(2)
  config.setConnectionTimeout(300000)
  //config.setConnectionTimeout(600000)
  //config.addDataSourceProperty("tcpKeepAlive", true);
  config.addDataSourceProperty("cachePrepStmts", "false");
  config.addDataSourceProperty("prepStmtCacheSize", "0");
  config.addDataSourceProperty("prepStmtCacheSqlLimit", "0");
  config.addDataSourceProperty("socketTimeout", 2147484)
  //val ds: HikariDataSource = new HikariDataSource(config);

  def enable_traz_f(count1: Int): Boolean = {
    if (count1 % 100 == 0) {
      log.error("CUMBIA count1 enable " + count1)
    }

    var count = count1
    Thread.sleep(10000)
    enable_traz match {
      case x if x.equals("true") => true
      case x if x.equals("false") => {
        count += 1
        enable_traz_f(count)
      }
    }
  }

  def connect(count1: Int): Connection = {
    if (count1 % 100 == 0) {
      log.error("CUMBIA count1 " + count1)
    }

    var count = count1
    //Thread.sleep(10000)

    try {
      //println("CUMBIA connect post Thread ")
      val c: Connection = new HikariDataSource(config).getConnection()
      //println("CUMBIA connect true")
      c
    }
    catch {
      case ex: Throwable =>
        count += 1
        //println("CUMBIA connect false")
        log.error("CUMBIA open connection to timescaledb 2" + ex.getMessage + " - " + ex.getCause)
        connect(count)

    }
  }
}