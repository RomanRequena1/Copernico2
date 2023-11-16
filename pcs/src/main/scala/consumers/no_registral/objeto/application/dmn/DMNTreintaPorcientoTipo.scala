package consumers.no_registral.objeto.application.dmn

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import org.camunda.dmn.DmnEngine

import java.io.FileInputStream
import scala.util.Try

object DMNTreintaPorcientoTipo {

  def dmn(obj: ObjetoExternalDto) = {

    val dmnStream = Try(new FileInputStream("/opt/docker/bin/clasificacion_objeto.dmn"))
    val engine = new DmnEngine()

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
      case None => None
    }
    val semaforo_color: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR
      case None => None
    }
    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
      .flatMap(dmn => engine.eval(dmn, "decision_0bpgmr3", Map("soj_tipo_objeto" -> obj.SOJ_TIPO_OBJETO,
        "soj_adherido_debito" -> obj.SOJ_ADHERIDO_DEBITO.getOrElse("None"),
        "soj_estado" -> obj.SOJ_ESTADO.getOrElse("None"),
        "soj_titularidad" -> obj.SOJ_TITULARIDAD.getOrElse("None"),
        "soj_semaforo" -> semaforo_color,
        "soj_marca" -> semaforo_marca)))
    chequeoDmn
  }
}
