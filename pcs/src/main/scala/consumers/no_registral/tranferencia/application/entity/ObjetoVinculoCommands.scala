package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.objeto.domain.ObjetoEvents.DmnResumen
import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait ObjetoVinculoCommands extends Command with ObjetoVinculoMessage with CbroSerialization


object ObjetoVinculoCommands {

  case class UpdateVinculoObjetoFromObj(
                                  deliveryId: BigInt,
                                  sujetoId: String,
                                  objetoId: String,
                                  tipoObj: String,
                                  tiene30Objeto: Boolean,
                                  isResponsable: Option[Boolean],
                                  estadoObj: Option[String],
                                  titularidad: Option[String],
                                  exclusionObjeto: Option[String],
                                  idExterno: Option[String],
                                  dmnNumero: Option[Int],
                                  dmnDescripcion: Option[String]
                                ) extends ObjetoVinculoCommands

  case class RemoveObjetoVinculo(
                                  deliveryId: BigInt,
                                  sujetoId: String,
                                  objetoId: String,
                                  tipoObj: String,
                                  tiene30Objeto: Boolean,
                                  isResponsable: Option[Boolean],
                                  estadoObj: Option[String],
                                  titularidad: Option[String],
                                  exclusionObjeto: Option[String]
                                ) extends ObjetoVinculoCommands


  case class CreateTransfVinculoObjetoFromObj(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30Objeto: Boolean,
                                          isResponsable: Option[Boolean],
                                          estadoObj: Option[String],
                                          titularidad: Option[String],
                                          exclusionObjeto: Option[String],
                                          idExterno: Option[String],
                                          dmnNumero: Option[Int],
                                          dmnDescripcion: Option[String]
                                             ) extends ObjetoVinculoCommands

  case class AuditarYEnviarResumen(
                                    deliveryId: BigInt,
                                    objetoId: String,
                                    tipoObj: String,
                                    sujetoId: String,
                                    aplicarDescuento: Option[Boolean],
                                    idExterno: Option[String],
                                    eventDmn: DmnResumen
                                  ) extends ObjetoVinculoCommands
}