package consumers.no_registral.tranferencia.domain

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObjetoVinculoEvent extends Event with ObjetoVinculoMessage with CbroSerialization {
  //TODO: validar pq se utiliza el tipoObj en el trait
  def tipoObj: String
  def deliveryId: BigInt
  override def aggregateRoot: String = s"ObjetoVinculo-$objetoId"
}

object ObjetoVinculoEvent {

  //FIXME: agregar deliveryId a todos los eventos de VinculoActor
  case class ObjetoVinculoSnapshotPersisted(
      objetoId: String,
      tipoObj: String,
      tiene30ObjetoVinculo: Boolean,
      mapTransf: Map[Vinculo, VinculoCotitular],
      mapVinculo: Map[Vinculo, VinculoCotitular],
      exclusionObjetoVinculo: Option[String],
      deliveryId: BigInt
  ) extends ObjetoVinculoEvent

  case class ResumenEnviado(
                             deliveryId: BigInt,
                             objetoId: String,
                             tipoObj: String,
                             aplicarDescuento: Option[Boolean],
                             fechaEnvio: java.time.LocalDateTime
                           ) extends ObjetoVinculoEvent

  case class UpdatedVinculoObjetoFromObj(
      sujetoId: String,
      objetoId: String,
      tipoObj: String,
      tiene30Objeto: Boolean,
      isResponsable: Option[Boolean],
      estadoObj: Option[String],
      titularidad: Option[String],
      exclusionObjeto: Option[String],
      deliveryId: BigInt,
      dmnNumero: Option[Int] = None,
      dmnDescripcion: Option[String] = None
  ) extends ObjetoVinculoEvent

  case class RemovedVinculoObjetoFromObj(
      sujetoId: String,
      objetoId: String,
      tipoObj: String,
      tiene30Objeto: Boolean,
      isResponsable: Option[Boolean],
      estadoObj: Option[String],
      titularidad: Option[String],
      excusionObjeto: Option[String],
      deliveryId: BigInt
  ) extends ObjetoVinculoEvent



  case class CreatedTransfVinculoObjetoFromObj(
      sujetoId: String,
      objetoId: String,
      tipoObj: String,
      tiene30Objeto: Boolean,
      isResponsable: Option[Boolean],
      estadoObj: Option[String],
      titularidad: Option[String],
      exclusionObjeto: Option[String],
      deliveryId: BigInt
  ) extends ObjetoVinculoEvent {

    case class UpdatedVinculoObjetoFromObj(
        sujetoId: String,
        objetoId: String,
        tipoObj: String,
        tiene30Objeto: Boolean,
        isResponsable: Option[Boolean],
        estadoObj: Option[String],
        titularidad: Option[String],
        deliveryId: BigInt
    ) extends ObjetoVinculoEvent

    case class CreatedTransfVinculoObjetoFromObj(
        sujetoId: String,
        objetoId: String,
        tipoObj: String,
        tiene30Objeto: Boolean,
        isResponsable: Option[Boolean],
        estadoObj: Option[String],
        titularidad: Option[String],
        deliveryId: BigInt
    ) extends ObjetoVinculoEvent
  }
}
