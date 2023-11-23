package consumers.no_registral.objeto.application.dmn

import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.application.helper.QueryExclusionObjeto
import consumers.no_registral.objeto.domain.ObjetoState
import org.camunda.dmn.DmnEngine

import java.io.FileInputStream
import scala.util.Try

object DMNTreintaPorcientoTipo {

  def dmn(obj: ObjetoState): Either[Product, DmnEngine.EvalResult] = {
    val exclusionObjeto = QueryExclusionObjeto(obj.registro.get.SOJ_IDENTIFICADOR)
    val isExclusionObjeto = if(exclusionObjeto.isEmpty) "" else exclusionObjeto.head
    val dmnStream = Try(new FileInputStream("/opt/docker/bin/clasificacion_objeto.dmn"))
    val engine = new DmnEngine()

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
      case None => None
    }
    val semaforo_color = obj.registro.get.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR
    println("HELP 3.1" + obj.registro + " - " + obj)
    println("HELP 3 -> semaforo_color " + semaforo_color + " - SOJ_TIPO_OBJETO " + obj.registro.get.SOJ_TIPO_OBJETO + " - SOJ_ADHERIDO_DEBITO" + obj.registro.get.SOJ_ADHERIDO_DEBITO.getOrElse("None") + " - SOJ_ESTADO " + obj.registro.get.SOJ_ESTADO.getOrElse("None") + " - SOJ_TITULARIDAD" + obj.registro.get.SOJ_TITULARIDAD.getOrElse("None") + " - isExclusionObjeto" + isExclusionObjeto)


    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
      .flatMap(dmn => engine.eval(dmn, "Decision_Clasificacion_Objeto", Map("soj_tipo_objeto" -> obj.registro.get.SOJ_TIPO_OBJETO,
        "soj_adherido_debito" -> obj.registro.get.SOJ_ADHERIDO_DEBITO.getOrElse("None"),
        "soj_estado" -> obj.registro.get.SOJ_ESTADO.getOrElse("None"),
        "soj_titularidad" -> obj.registro.get.SOJ_TITULARIDAD.getOrElse("None"),
        "soj_semaforo" -> semaforo_color.getOrElse("None"),
        "soj_exclusionObjeto" -> isExclusionObjeto)))
    chequeoDmn
  }
}
