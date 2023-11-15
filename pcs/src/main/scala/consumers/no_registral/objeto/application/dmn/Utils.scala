package consumers.no_registral.objeto.application.dmn

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{ListDetallesObjeto, ObjetosTri}

object Utils {

  def parser(obj: ObjetosTri): Option[Seq[ListDetallesObjeto]] = {
    val detalles = for {
      otrosAtributos <- obj.SOJ_OTROS_ATRIBUTOS
      objDetalles: JsValue <- (otrosAtributos \ "SOJ_DETALLES").toOption
      detalles: Seq[ObjetosTriOtrosAtributos] = serialization.decodeF[Seq[ObjetosTriOtrosAtributos]](objDetalles.toString)
    } yield (detalles)
    detalles
  }

}

