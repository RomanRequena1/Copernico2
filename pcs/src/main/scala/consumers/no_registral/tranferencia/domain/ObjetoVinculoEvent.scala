package consumers.no_registral.tranferencia.domain

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObjetoVinculoEvent extends Event with ObjetoVinculoMessage with CbroSerialization {
  def tipoObj: String
}


object ObjetoVinculoEvent {

  //FIXME: agregar deliveryId a todos los eventos de VinculoActor
  case class ObjetoVinculoSnapshotPersisted(
                                             objetoId: String,
                                             tipoObj: String,
                                             tiene30ObjetoVinculo: Boolean,
                                             mapTransf: Map[Vinculo, VinculoCotitular],
                                             mapVinculo: Map[Vinculo, VinculoCotitular],
                                             exclusionObjetoVinculo: Option[String]
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
                                        deliveryId: BigInt
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
                                           ) extends ObjetoVinculoEvent{

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
