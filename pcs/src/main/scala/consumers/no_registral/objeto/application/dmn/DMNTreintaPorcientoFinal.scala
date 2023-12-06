package consumers.no_registral.objeto.application.dmn

import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.objeto.domain.ObjetoState
import org.camunda.dmn.DmnEngine

import java.io.FileInputStream
import scala.util.Try

object DMNTreintaPorcientoFinal {

  def dmn( state: ObjetoState, cmd: ObjetoUpdateFromSujeto, exclusionObjeto: Seq[String]): Either[Product, DmnEngine.EvalResult] = {
    val path: String = Try(System.getenv("PATH_DMN_OBJETO_CUPON")).getOrElse("")
    val dmnId: String = Try(System.getenv("DMN_ID_OBJETO_CUPON")).getOrElse("id")
    val dmnStream = Try(new FileInputStream(path))
    val engine = new DmnEngine()
    val isExclusionObjeto = if(exclusionObjeto.isEmpty) "" else exclusionObjeto.head

    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
      .flatMap(dmn => engine.eval(dmn, dmnId, Map(
        "suj_exclusionSujeto" -> cmd.exclusionSUjeto,
        "soj_exclusionObjeto" -> isExclusionObjeto,
        "soj_clasificacionObjeto" -> state.clasificacionObjeto,
        "suj_deuda30Sujeto" -> state.deuda30Sujeto.get.toString,
        "soj_deuda30Objeto" -> state.deuda30Objeto.toString
      )))
    chequeoDmn
  }
}
