package readside.proyectionists.no_registrales.objeto.projections

import consumers.no_registral.objeto.application.entities.{ObjetosTri, ObjetosTriOtrosAtributos}
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import io.circe
import org.slf4j.LoggerFactory
import io.circe.syntax.EncoderOps
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
case class ObjetoSnapshotPersistedProjection(
    event: ObjetoSnapshotPersisted
) extends ObjetoProjection {
  val registro: Option[ObjetosTri] = event.registro
  val test: Option[Map[String, String]] = registro match {
    case Some(value) => {
      val sojDetailsResult: Option[Map[String, List[ObjetosTriOtrosAtributos]]] = circe.jawn.decode[Map[String, List[ObjetosTriOtrosAtributos]]](registro.get.SOJ_OTROS_ATRIBUTOS.asJson.toString()).toOption
      println("CUMBIA bobDetailsResult -> " + sojDetailsResult)
      val mao: Map[String, String] = Map("SOJ_DETALLES" -> sojDetailsResult.get("SOJ_DETALLES").asJson.noSpaces)
      Some(mao)}

    case None => None
  }




  println("CUMBIAA " + registro)
  val fromRegistro: Option[List[(String, Option[Object])]] = registro match {
    case Some(r) => Some(List(
      "soj_identificador_2" -> r.SOJ_IDENTIFICADOR_2,
      "soj_subtipo" -> r.SOJ_SUBTIPO,
      "soj_canal_origen" -> r.SOJ_CANAL_ORIGEN,
      "soj_cat_soj_id" -> r.SOJ_CAT_SOJ_ID,
      "soj_descripcion" -> r.SOJ_DESCRIPCION,
      "soj_estado" -> r.SOJ_ESTADO,
      "soj_fecha_fin" -> r.SOJ_FECHA_FIN,
      "soj_fecha_inicio" -> r.SOJ_FECHA_INICIO,
      "soj_id_externo" -> r.SOJ_ID_EXTERNO,
      "soj_otros_atributos" -> test,
      "soj_base_imponible" -> r.SOJ_BASE_IMPONIBLE,
      "soj_adherido_debito" -> r.SOJ_ADHERIDO_DEBITO,
      "soj_cant_cuotas_pagadas" -> Some(event.cuotas.mkString("[",",","]")),
      "soj_titularidad" -> r.SOJ_TITULARIDAD
  ))
    case None => Some(List(
      "soj_descripcion" -> Some("Sin descripción"),
      "soj_id_externo" -> event.idExterno,
      "soj_identificador_2" -> event.objetoId2,
      "soj_cant_cuotas_pagadas" -> Some(event.cuotas.mkString("[",",","]"))
    ))
  }

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
