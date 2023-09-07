package consumers.registral.componente_i.domain

import consumers.registral.componente_i.application.entities.ComponenteIExternalDto.DetallesComponenteI
import consumers.registral.componente_i.application.entities.{ComponenteIExternalDto, ComponenteIMessage}
import design_principles.actor_model.Event

sealed trait ComponenteIEvents extends Event with ComponenteIMessage {
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
                                          registro: Option[ComponenteIExternalDto],
                                        ) extends ComponenteIEvents
  case class ComponenteIUpdatedFromDto(
                                       deliveryId: BigInt,
                                       sujetoId: String,
                                       objetoId: String,
                                       tipoObjeto: String,
                                       obligacionId: String,
                                       registro: ComponenteIExternalDto,
                                       detallesComponenteI: Seq[DetallesComponenteI],
                                     ) extends ComponenteIEvents

  case class ComponenteIRemoved(
                                deliveryId: BigInt,
                                sujetoId: String,
                                objetoId: String,
                                tipoObjeto: String,
                                obligacionId: String,
                                registro: ComponenteIExternalDto,
                              ) extends ComponenteIEvents
}