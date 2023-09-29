package consumers.registral.subasta.application.entities

import serialization.CbroSerialization

sealed trait SubastaCommands extends design_principles.actor_model.Command with SubastaMessage with CbroSerialization
object SubastaCommands {
  case class SubastaUpdateFromDto(sujetoId: String,
                                  objetoId: String,
                                  tipoObjeto: String,
                                  subastaId: String,
                                  deliveryId: BigInt,
                                  registro: SubastaExternalDto)
      extends SubastaCommands

}
