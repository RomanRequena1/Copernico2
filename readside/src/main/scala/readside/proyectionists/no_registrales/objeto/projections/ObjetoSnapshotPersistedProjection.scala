package readside.proyectionists.no_registrales.objeto.projections

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.DetallesObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import io.circe.parser._
import io.circe.syntax.EncoderOps

case class ObjetoSnapshotPersistedProjection(
    event: ObjetoSnapshotPersisted
) extends ObjetoProjection {

  def tipoANT(tipo: String, descripcion: Option[String]) = {
    descripcion match {
      case Some(value) => value.equals("DefaultANT") || (
        tipo == "PPP" ||
          tipo == "PM26" ||
          tipo == "PVS" ||
          tipo == "MVD" ||
          tipo == "LTE" ||
          tipo == "BDG"
        )
      case None => false
    }

  }

  val registro: Option[ObjetoExternalDto] = event.registro
  val fromRegistro: Option[List[(String, Option[Any])]] = registro match {
    case Some(r) if tipoANT(r.SOJ_TIPO_OBJETO, r.SOJ_DESCRIPCION) => {
      Some(
        List(
          "soj_descripcion" -> Some("Sin descripción"),
          "soj_id_externo" -> event.idExterno,
          "soj_identificador_2" -> event.objetoId2,
          "soj_cant_cuotas_pagadas" -> Some(event.cuotas.mkString("[", ",", "]")),
          "soj_tiene30Objeto" -> event.tiene30Objeto,
          "soj_tiene30ObjetoVinculo" -> event.tiene30ObjetoVinculo,
          "soj_aplicarDescuento" -> event.aplicarDescuento,
          "soj_resultDmn" -> Some(event.bandTipo.toInt)
        )
      )
    }
    case Some(r) =>
      val bobDetailsResult: Option[Map[String, List[DetallesObjeto]]] = {
        decode[Map[String, List[DetallesObjeto]]](registro.get.SOJ_OTROS_ATRIBUTOS.asJson.toString()).toOption
      }
      val mao = bobDetailsResult match {
        case Some(value) => Map("SOJ_DETALLES" -> value.get("SOJ_DETALLES").asJson.noSpaces)
        case None => None
      }
      Some(
        List(
          "soj_identificador_2" -> r.SOJ_IDENTIFICADOR_2,
          "soj_documento" -> r.SOJ_DOCUMENTO,
          "soj_subtipo" -> r.SOJ_SUBTIPO,
          "soj_canal_origen" -> r.SOJ_CANAL_ORIGEN,
          "soj_cat_soj_id" -> r.SOJ_CAT_SOJ_ID,
          "soj_descripcion" -> r.SOJ_DESCRIPCION,
          "soj_estado" -> r.SOJ_ESTADO,
          "soj_fecha_fin" -> r.SOJ_FECHA_FIN,
          "soj_fecha_inicio" -> r.SOJ_FECHA_INICIO,
          "soj_id_externo" -> r.SOJ_ID_EXTERNO,
          "soj_otros_atributos" -> Some(mao),
          "soj_base_imponible" -> r.SOJ_BASE_IMPONIBLE,
          "soj_adherido_debito" -> r.SOJ_ADHERIDO_DEBITO,
          "soj_cant_cuotas_pagadas" -> Some(event.cuotas.mkString("[", ",", "]")),
          "soj_tipo_exclusion" -> event.exclusionObjeto,
          "soj_titularidad" -> r.SOJ_TITULARIDAD,
          "soj_tiene30Objeto" -> event.tiene30Objeto,
          "soj_tiene30ObjetoVinculo" -> event.tiene30ObjetoVinculo,
          "soj_aplicarDescuento" -> event.aplicarDescuento,
          "soj_resultDmn" -> Some(event.bandTipo.toInt),
          "soj_fecha_vta_subasta" -> r.SOJ_FECHA_VTA_SUBASTA,
          "soj_fecha_adq_subasta" -> r.SOJ_FECHA_ADQ_SUBASTA
        )
      )
    case None =>
      Some(
        List(
          "soj_descripcion" -> Some("Sin descripción"),
          "soj_id_externo" -> event.idExterno,
          "soj_identificador_2" -> event.objetoId2,
          "soj_cant_cuotas_pagadas" -> Some(event.cuotas.mkString("[", ",", "]")),
          "soj_tiene30Objeto" -> event.tiene30Objeto,
          "soj_tiene30ObjetoVinculo" -> event.tiene30ObjetoVinculo,
          "soj_aplicarDescuento" -> event.aplicarDescuento,
          "soj_resultDmn" -> Some(event.bandTipo.toInt)
        )
      )
  }
// FIXME: quitar el others y agregar el soj_saldo donde corresponda
  val others: List[(String, BigDecimal)] = List(
    "soj_saldo" -> event.saldo
    //"soj_vencida" -> event.vencimiento,
    //"soj_cotitular_suj_identificador" -> event.sujetoResponsable,
    //"soj_etiquetas" -> event.tags.mkString(","), //set(snapshot.tags),
    //"soj_cotitulares" -> event.cotitulares,
    //"soj_porcentaje_cotitular" -> event.porcentajeResponsabilidad
  )

  val bindings: List[(String, Serializable)] = fromRegistro match {
    case Some(optionalAttributes) => optionalAttributes ++ others
    case None => others
  }

}
