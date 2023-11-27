package consumers.no_registral.objeto.application.helper

import com.datastax.oss.driver.api.core.CqlSession

object QueryExclusionObjeto {
  def apply(objetoId: String): Seq[String] = {
    val session: CqlSession = CqlSession.builder().build()

    val query = s"SELECT * FROM read_side.buc_exclusiones_objeto WHERE boe_obj_id = '${objetoId}';" // Reemplazar 'Alice' con el valor deseado

    val resultSet = session.execute(query)

    import scala.jdk.CollectionConverters._

    //val result = resultSet.all().asScala.toList
    val userList = resultSet.all().asScala.toList.map { row =>

        row.getString("boe_tipo_exclusion")


    }
    session.close()

    userList


  }
}
