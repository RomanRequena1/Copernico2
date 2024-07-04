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
      case x if x.soj_estado.equals("TRANSF")  => 1
      case x if x.soj_exclusionObjeto.equals("C") => 1
      case x if x.soj_estado.equals("ESTADO2") => 1
      case x if x.soj_tipo_objeto.equals("I") && (x.soj_semaforo.equals("A") || x.soj_semaforo.equals("R")) => 1
      case x if x.soj_titularidad.equals("CONDOMINO") || x.soj_titularidad.equals("CONDOMINIO") || x.soj_titularidad.equals("CONDOMINO-P") || x.soj_titularidad.equals("CONDOMINIO-P") => 1
      case _ => 2

    }
  }
//  def dmn(obj: ObjetoUpdateFromTri):Any = {
//    val path: String = Try(System.getenv("PATH_DMN_CLASIF_OBJETO")).getOrElse("")
//    val dmnId: String = Try(System.getenv("DMN_ID_CLASIF_OBJETO")).getOrElse("id")
//    val exclusionObjeto = QueryExclusionObjeto(obj.registro.SOJ_IDENTIFICADOR)
//    val isExclusionObjeto = if(exclusionObjeto.isEmpty) "" else exclusionObjeto.head
//    val dmnStream = Try(new FileInputStream(path))
//    val engine = new DmnEngine()
//
//    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
//      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
//      case None => None
//    }
//    val semaforo_color = obj.registro.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR
//
//
//    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
//      .flatMap(dmn => engine.eval(dmn, dmnId, Map("soj_tipo_objeto" -> obj.registro.SOJ_TIPO_OBJETO,
//        "soj_adherido_debito" -> obj.registro.SOJ_ADHERIDO_DEBITO.getOrElse("None"),
//        "soj_estado" -> obj.registro.SOJ_ESTADO.getOrElse("None"),
//        "soj_titularidad" -> obj.registro.SOJ_TITULARIDAD.getOrElse("None"),
//        "soj_semaforo" -> semaforo_color.getOrElse("None"),
//        "soj_exclusionObjeto" -> isExclusionObjeto)))
//    chequeoDmn.fold(e => log.error("ERROR DMN OBJETO::" + e), value => value.value)
//  }
}

//object testDmnScala extends App {
//  def calcularDmn(
//                   suj_exclusionSujeto: String,
//                   soj_exclusionObjeto: String,
//                   soj_clasificacionObjeto: String,
//                   soj_deuda30Objeto: Boolean,
//                   suj_deuda30Sujeto: Boolean) = {
//
//
//    if (suj_exclusionSujeto == "E")
//       true
//    else if (suj_exclusionSujeto == "NE")
//       false
//    else if (soj_exclusionObjeto == "E")
//       true
//    else if (soj_exclusionObjeto == "NE")
//       false
//    else if (soj_clasificacionObjeto == "1" && soj_deuda30Objeto)
//       true
//    else if (soj_clasificacionObjeto == "1" && !soj_deuda30Objeto)
//       false
//    else if (soj_clasificacionObjeto == "2" && suj_deuda30Sujeto)
//       true
//    else if (soj_clasificacionObjeto == "2" && !suj_deuda30Sujeto)
//       false
//  }
//
//  val suj_exclusionSujeto = ""
//  val soj_exclusionObjeto = ""
//  val soj_clasificacionObjeto = ""
//  val suj_deuda30Sujeto = true
//  val soj_deuda30Objeto = true
//println(calcularDmn(suj_exclusionSujeto, soj_exclusionObjeto, soj_clasificacionObjeto, suj_deuda30Sujeto, soj_deuda30Objeto))
//}
