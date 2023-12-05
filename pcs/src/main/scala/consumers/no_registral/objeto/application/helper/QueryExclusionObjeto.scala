package consumers.no_registral.objeto.application.helper

import com.datastax.oss.driver.api.core.CqlSession

object QueryExclusionObjeto {
  def apply(objetoId: String): Seq[String] = { // todo devolver solo el tipo
    val session: CqlSession = CqlSession.builder().build()

    val query = s"SELECT * FROM read_side.buc_objeto_exclusiones WHERE bse_soj_identificador = '${objetoId}';" // Reemplazar 'Alice' con el valor deseado

    val resultSet = session.execute(query)

    import scala.jdk.CollectionConverters._

    //val result = resultSet.all().asScala.toList
    val userList = resultSet.all().asScala.toList.map { row =>

        row.getString("bse_tipo_exclusion")


    }
    session.close()

    userList // todo devuelve un seq(tipo, fecha)
    //todo tomas la fecha y la usas para validad si menor a la actual, si es el caso, devolver el tipo. Si no lo es, devolver un string vacio ""



  }
}
