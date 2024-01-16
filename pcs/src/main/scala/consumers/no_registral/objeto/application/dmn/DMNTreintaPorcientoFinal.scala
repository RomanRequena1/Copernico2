package consumers.no_registral.objeto.application.dmn

import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.objeto.domain.ObjetoState
import org.camunda.dmn.DmnEngine

import java.io.FileInputStream
import scala.util.Try

object DMNTreintaPorcientoFinal {
  case class DmnFinal(suj_exclusionSujeto: String,
                      soj_exclusionObjeto: String,
                      soj_clasificacionObjeto: String,
                      soj_deuda30Objeto: Boolean,
                      suj_deuda30Sujeto: Boolean)
  def calcularDmnFinal(dmn:DmnFinal): Boolean = {
    dmn match {
      case x if x.suj_exclusionSujeto.equals("E") => true
      case x if x.suj_exclusionSujeto.equals("NE") => false
      case x if x.soj_exclusionObjeto.equals("E") => true
      case x if x.soj_exclusionObjeto.equals("NE") => false
      case x if x.soj_clasificacionObjeto.equals("1") && x.soj_deuda30Objeto => true
      case x if x.soj_clasificacionObjeto.equals("1") && !x.soj_deuda30Objeto => false
      case x if x.soj_clasificacionObjeto.equals("2") && x.suj_deuda30Sujeto => true
      case x if x.soj_clasificacionObjeto.equals("2") && !x.suj_deuda30Sujeto => false
      case _ => true
    }
  }

//  def dmn( state: ObjetoState, cmd: ObjetoUpdateFromSujeto, exclusionObjeto: String): Either[Product, DmnEngine.EvalResult] = {
//    val path: String = Try(System.getenv("PATH_DMN_OBJETO_CUPON")).getOrElse("")
//    val dmnId: String = Try(System.getenv("DMN_ID_OBJETO_CUPON")).getOrElse("id")
//    val dmnStream = Try(new FileInputStream(path))
//    val engine = new DmnEngine()
//    //val isExclusionObjeto = if(exclusionObjeto.isEmpty) "" else exclusionObjeto.head
//
//    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
//      .flatMap(dmn => engine.eval(dmn, dmnId, Map(
//        "suj_exclusionSujeto" -> cmd.exclusionSUjeto,
//        "soj_exclusionObjeto" -> "",
//        "soj_clasificacionObjeto" -> state.clasificacionObjeto,
//        "suj_tiene30Sujeto" -> state.tiene30Sujeto.get.toString,
//        "soj_tiene30Objeto" -> state.tiene30Objeto.toString
//      )))
//    chequeoDmn
//  }
}
