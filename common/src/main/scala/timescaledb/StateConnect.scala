package timescaledb

import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection

case class StateConnect(
                       conn: Option[Connection] = None
                       )


case class Reconnet(
                   status: Boolean,
                   conn: Option[Connection]
                   )