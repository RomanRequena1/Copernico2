package consumers.registral.componente_i.domain

import consumers.registral.componente_i.application.entities.{ComponenteIMessage, ComponenteITri, DetallesComponenteI}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ComponenteIEvents extends Event with ComponenteIMessage with CbroSerialization{
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def obligacionId: String
}


object ComponenteIEvents {

  case class ComponenteIPersistedSnapshot(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObjeto: String,
                                          obligacionId: String,
                                          registro: Option[ComponenteITri],
                                        ) extends ComponenteIEvents
  case class ComponenteIUpdatedFromDto(
                                       deliveryId: BigInt,
                                       sujetoId: String,
                                       objetoId: String,
                                       tipoObjeto: String,
                                       obligacionId: String,
                                       registro: ComponenteITri,
                                       detallesComponenteI: Seq[DetallesComponenteI],
                                     ) extends ComponenteIEvents

  case class ComponenteIRemoved(
                                deliveryId: BigInt,
                                sujetoId: String,
                                objetoId: String,
                                tipoObjeto: String,
                                obligacionId: String,
                                registro: ComponenteITri,
                              ) extends ComponenteIEvents
}