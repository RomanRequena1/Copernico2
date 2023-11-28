package consumers.no_registral.sujeto.application.helper

import com.datastax.oss.driver.api.core.CqlSession

object QueryExclusionSujeto {
  def apply(sujetoId: String): Seq[String] = {
    val session: CqlSession = CqlSession.builder().build()

    val query = s"SELECT * FROM read_side.buc_sujeto_exclusiones WHERE bse_suj_identificador = '${sujetoId}';" // Reemplazar 'Alice' con el valor deseado

    val resultSet = session.execute(query)

    import scala.jdk.CollectionConverters._

    //val result = resultSet.all().asScala.toList
    val userList = resultSet.all().asScala.toList.map { row =>

        row.getString("bse_tipo_exclusion")


    }
    session.close()

    userList


  }
}
