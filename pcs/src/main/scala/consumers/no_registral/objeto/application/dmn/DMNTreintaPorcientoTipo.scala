package consumers.no_registral.objeto.application.dmn

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory

object DMNTreintaPorcientoTipo {
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)

  case class DmnObjeto(soj_tipo_objeto: String,
                       soj_adherido_debito: String,
                       soj_estado: String,
                       soj_titularidad: String,
                       soj_semaforo: String,
                       soj_exclusionObjeto: String,
                       clasificacionObjeto: String)

  // FIXME: Analizar casos de condominio con 1 solo poseedor, llega en soj_estado = null y soj_titularidad = null
  def calcularDmn(dmn: DmnObjeto): Int = {
    dmn match {
      //case x if x.soj_tipo_objeto.equals("M") => 2 // todo en caso de que el tipo de objeto sea M  debe inpactar en el 30
      case x if x.soj_estado.equals("BAJA") => 1 // todo en caso de que el estado sea baja debe impactar en el 30
      case x if x.soj_estado.equals("TRANSF") => 1
      case x if x.soj_exclusionObjeto.equals("C") => 1
      case x if x.soj_estado.equals("ESTADO2") => 1
      case x if x.soj_tipo_objeto.equals("I") && (x.soj_semaforo.equals("A") || x.soj_semaforo.equals("R")) => 1
      case x
          if x.soj_titularidad.equals("CONDOMINO") || x.soj_titularidad.equals("CONDOMINIO") || x.soj_titularidad
            .equals("CONDOMINO-P") || x.soj_titularidad.equals("CONDOMINIO-P") =>
        1
      case _ => 2

    }
  }
}
