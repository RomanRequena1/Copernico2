package consumers.registral.juicio_obn.domain

import consumers.registral.juicio_obn.application.entities.{JuicioObnMessage, JuicioObnTri}
import design_principles.actor_model.Event
import serialization.CbroSerialization


sealed trait JuicioObnEvents extends Event with JuicioObnMessage with CbroSerialization


object JuicioObnEvents {
  case class JuicioObnUpdatedFromDto(
                                      deliveryId: BigInt,
                                      juicioObnId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      obligacionId: String,
                                      registro: JuicioObnTri
                                    ) extends JuicioObnEvents

  case class JuicioObnDeletedFromDto(
                                      deliveryId: BigInt,
                                      juicioObnId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      obligacionId: String,
                                      registro: JuicioObnTri
                                    ) extends JuicioObnEvents

}
