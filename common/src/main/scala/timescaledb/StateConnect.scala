package timescaledb


import java.sql.Connection

case class StateConnect(
                       conn: Option[Connection] = None
                       )

