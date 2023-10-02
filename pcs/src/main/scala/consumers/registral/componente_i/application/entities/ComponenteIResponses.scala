
package consumers.registral.componente_i.application.entities


import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ComponenteIResponses extends CbroSerialization


object ComponenteIResponses {

  case class GetComponenteIResponse(registro: Option[ComponenteITri] = None,
                               detallesComponenteI: Seq[DetallesComponenteI],
                               fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response with CbroSerialization

}