package consumers.no_registral.objeto.application.dmn

import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.application.helper.QueryExclusionObjeto
import consumers.no_registral.objeto.domain.ObjetoState
import org.camunda.dmn.DmnEngine
import org.slf4j.LoggerFactory

import java.io.FileInputStream
import scala.util.Try

object DMNTreintaPorcientoTipo {
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)
  def dmn(obj: ObjetoUpdateFromTri):Any = {
    val exclusionObjeto = QueryExclusionObjeto(obj.registro.SOJ_IDENTIFICADOR)
    val isExclusionObjeto = if(exclusionObjeto.isEmpty) "" else exclusionObjeto.head
    val dmnStream = Try(new FileInputStream("/opt/docker/bin/clasificacion_objeto.dmn"))
    val engine = new DmnEngine()

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
      case None => None
    }
    val semaforo_color = obj.registro.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR
    println("HELP 3.1" + obj.registro + " - " + obj)
    println("HELP 3 -> semaforo_color " + semaforo_color + " - SOJ_TIPO_OBJETO " + obj.registro.SOJ_TIPO_OBJETO + " - SOJ_ADHERIDO_DEBITO" + obj.registro.SOJ_ADHERIDO_DEBITO.getOrElse("None") + " - SOJ_ESTADO " + obj.registro.SOJ_ESTADO.getOrElse("None") + " - SOJ_TITULARIDAD" + obj.registro.SOJ_TITULARIDAD.getOrElse("None") + " - isExclusionObjeto" + isExclusionObjeto)


    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
      .flatMap(dmn => engine.eval(dmn, "Decision_Clasificacion_Objeto", Map("soj_tipo_objeto" -> obj.registro.SOJ_TIPO_OBJETO,
        "soj_adherido_debito" -> obj.registro.SOJ_ADHERIDO_DEBITO.getOrElse("None"),
        "soj_estado" -> obj.registro.SOJ_ESTADO.getOrElse("None"),
        "soj_titularidad" -> obj.registro.SOJ_TITULARIDAD.getOrElse("None"),
        "soj_semaforo" -> semaforo_color.getOrElse("None"),
        "soj_exclusionObjeto" -> isExclusionObjeto)))
    chequeoDmn.fold(e => log.error("ERROR DMN OBJETO::" + e), value => value.value)
  }
}
