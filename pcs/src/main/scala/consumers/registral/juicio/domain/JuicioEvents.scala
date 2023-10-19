package consumers.registral.juicio.domain

import consumers.registral.juicio.application.entities.JuicioExternalDto.DetallesJuicio
import consumers.registral.juicio.application.entities.{JuicioExternalDto, JuicioMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait JuicioEvents extends Event with JuicioMessage with CbroSerialization

object JuicioEvents {
  case class JuicioUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      juicioId: String,
      registro: JuicioExternalDto,
      detallesJuicio: Seq[DetallesJuicio]
  ) extends JuicioEvents
}
