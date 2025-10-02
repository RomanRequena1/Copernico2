package consumers.no_registral.objeto.domain

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import design_principles.actor_model.Event
import org.camunda.feel.LocalDateTime
import serialization.CbroSerialization

sealed trait ObjetoEvents extends Event with CbroSerialization {
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def deliveryId: BigInt
  def aggregateRoot: String = s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto"
}

object ObjetoEvents {
  val operaciones: Map[String, String] = Map(("Upsert" -> "U"), ("Delete" -> "D"))

  case class ObjetoUpdatedCotitulares(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      cotitulares: Set[String]
  ) extends ObjetoEvents

  case class ObjetoUpdatedFromSujeto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      tiene30Sujeto: Boolean,
      exclusionSUjeto: Option[String],
      dmnDescripcionSujeto: Option[String]
                                    ) extends ObjetoEvents

  case class AplicarDescuentoUpdated(
                                         deliveryId: BigInt,
                                         sujetoId: String,
                                         objetoId: String,
                                         tipoObjeto: String,
                                         aplicarDescuento: Option[Boolean]
                                       ) extends ObjetoEvents

  case class DmnResumen(
                                      deliveryId: BigInt,
                                      sujetoId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      idExterno: Option[String],
                                      fecha: Option[LocalDateTime],
                                      aplicarDescuento: Option[Boolean],
                                      dmnNumero: Option[Int],
                                      dmnDescripcion: Option[String]
                       ) extends ObjetoEvents

  case class DmnResumenSnapshotPersisted(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObjeto: String,
                                          idExterno: Option[String],
                                          fecha: Option[LocalDateTime],
                                          beneficios: Seq[Beneficio]
                                        ) extends ObjetoEvents


  case class Beneficio(
                        codigo: String,
                        aplicarDescuento: Option[Boolean],
                        dmnNumero: Option[Int],
                        dmnDescripcion: Option[String]
                      )

  case class ObjetoSnapshotPersisted(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      objetoId2: Option[String],
      tipoObjeto: String,
      saldo: BigDecimal,
      cotitulares: Set[String],
      tags: Set[String],
      sujetoResponsable: Option[String],
      porcentajeResponsabilidad: BigDecimal,
      registro: Option[ObjetoExternalDto],
      obligacionesSaldo: Map[String, BigDecimal] = Map.empty,
      cuotas: List[Boolean],
      bandTipo: String,
      operacion: String,
      idExterno: Option[String],
      tiene30Objeto: Option[Boolean],
      aplicarDescuento: Option[Boolean],
      resultDmn: Int,
      exclusionObjeto: Option[String],
      tiene30ObjetoVinculo: Option[Boolean]
  ) extends ObjetoEvents

  case class ObjetoUpdatedFromTri(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      registro: ObjetoExternalDto,
      isResponsable: Option[Boolean],
      sujetoResponsable: Option[String],
      isAdheridoDebito: Option[Boolean],
      clasificacionObjeto: Option[String],
      resultDmn: Option[Int]
  ) extends ObjetoEvents

  case class ObjetoUpdatedFromAnt(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      registro: ObjetoExternalDto,
      isResponsable: Option[Boolean],
      sujetoResponsable: Option[String],
      isAdheridoDebito: Option[Boolean]
  ) extends ObjetoEvents

  case class ObjetoTagAdded(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      tagAdded: String
  ) extends ObjetoEvents

  case class ObjetoTagRemoved(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      tagRemoved: String
  ) extends ObjetoEvents

  case class UpdatedState30ObjetoFromObjVinculo(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      tiene30ObjetoVinculo: Boolean,
      exclusionObjetoVinculo: Option[String]
  ) extends ObjetoEvents
  case class ObjetoUpdatedFromObligacion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      objetoId2: Option[String],
      tipoObjeto: String,
      obligacionId: String,
      saldoObligacion: BigDecimal,
      obligacionExenta: Boolean,
      porcentajeExencion: Option[BigDecimal],
      idExterno: Option[String],
      cuota: Option[String],
      dmnNumero: Option[Int],
      dmnDescripcion: Option[String]
  ) extends ObjetoEvents

  case class ObjetoUpdatedFromObligacionBajaSet(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String
  ) extends ObjetoEvents

  case class ObjetoAddedExencion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      exencion: Exencion
  ) extends ObjetoEvents

  case class ObjetoBajaSet(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      registro: ObjetoExternalDto,
      isResponsable: Option[Boolean],
      sujetoResponsable: Option[String]
  ) extends ObjetoEvents

  case class ObjetoRemovedObligacion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      cuota: Option[String],
      dmnNumero: Option[Int],
      dmnDescripcion: Option[String]
  ) extends ObjetoEvents
  case class RemovedObjetoFromObligacion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      cuota: Option[String]
  ) extends ObjetoEvents

  case class ObjetoUpdatedFromObnTreintaProciento(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      objetoId2: Option[String],
      tipoObjeto: String,
      obligacionId: String,
      saldoObligacion: BigDecimal,
      obligacionExenta: Boolean,
      porcentajeExencion: Option[BigDecimal],
      idExterno: Option[String],
      cuota: Option[String],
      dmnNumero: Option[Int],
      dmnDescripcion: Option[String]
  ) extends ObjetoEvents

}
