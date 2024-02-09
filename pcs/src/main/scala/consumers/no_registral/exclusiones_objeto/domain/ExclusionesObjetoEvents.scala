package consumers.no_registral.exclusiones_objeto.domain

import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoMessage
import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoExternalDto
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ExclusionesObjetoEvents extends Event with ExclusionesObjetoMessage with CbroSerialization{



}


object ExclusionesObjetoEvents {

  case class ExclusionesObjetoUpdatedFromDto(
                                              deliveryId: BigInt,
                                              objetoId: String,
                                              registro: ExclusionesObjetoExternalDto)
    extends ExclusionesObjetoEvents

}