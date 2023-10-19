package consumers.registral.juicio.application.entities

import consumers.registral.juicio.application.entities.JuicioExternalDto.DetallesJuicio
import serialization.CbroSerialization

sealed trait JuicioCommands extends design_principles.actor_model.Command with JuicioMessage with CbroSerialization
object JuicioCommands {
  case class JuicioUpdateFromDto(sujetoId: String,
                                 objetoId: String,
                                 tipoObjeto: String,
                                 juicioId: String,
                                 deliveryId: BigInt,
                                 registro: JuicioExternalDto,
                                 detallesJuicio: Seq[DetallesJuicio])
      extends JuicioCommands

}
