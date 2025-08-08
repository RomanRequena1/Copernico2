package readside.proyectionists.no_registrales.objeto.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.DetallesObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import io.circe.parser._
import io.circe.syntax.EncoderOps
case class ObjetoDocumentoProjection(
    event: ObjetoSnapshotPersisted
) extends UpdateReadSideProjection[ObjetoSnapshotPersisted] {

  def collectionName: String = "read_side.buc_objeto_documento"

  private val sojDocumento: String = event.registro
    .flatMap(_.SOJ_DOCUMENTO)
    .getOrElse("")

  val keys: List[(String, Object)] = List(
    "soj_tipo_objeto" -> event.tipoObjeto,
    "soj_documento" -> sojDocumento,
    "soj_identificador" -> event.objetoId,
    "soj_suj_identificador" -> event.sujetoId
  )

  val registro: Option[ObjetoExternalDto] = event.registro

  val fromRegistro: Option[List[(String, Option[Any])]] = registro match {
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
          "soj_id_externo" -> r.SOJ_ID_EXTERNO,
          "soj_cat_soj_id" -> r.SOJ_CAT_SOJ_ID,
          "soj_descripcion" -> r.SOJ_DESCRIPCION,
          "soj_estado" -> r.SOJ_ESTADO,
          "soj_fecha_fin" -> r.SOJ_FECHA_FIN,
          "soj_fecha_inicio" -> r.SOJ_FECHA_INICIO,
          "soj_otros_atributos" -> Some(mao),
          "soj_saldo" -> Some(event.saldo),
          "soj_base_imponible" -> r.SOJ_BASE_IMPONIBLE,
          "soj_adherido_debito" -> r.SOJ_ADHERIDO_DEBITO,
          "soj_cant_cuotas_pagadas" -> Some(event.cuotas.mkString("[", ",", "]")),
          "soj_canal_origen" -> r.SOJ_CANAL_ORIGEN,
          "soj_subtipo" -> r.SOJ_SUBTIPO,
          "soj_identificador_2" -> r.SOJ_IDENTIFICADOR_2,
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
          "soj_saldo" -> Some(event.saldo),
          "soj_tiene30Objeto" -> event.tiene30Objeto,
          "soj_tiene30ObjetoVinculo" -> event.tiene30ObjetoVinculo,
          "soj_aplicarDescuento" -> event.aplicarDescuento,
          "soj_resultDmn" -> Some(event.bandTipo.toInt)
        )
      )
  }

  val bindings: List[(String, Serializable)] = fromRegistro.getOrElse(List.empty)
}
