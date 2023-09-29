package consumers.registral.etapas_procesales.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait EtapasProcesalesResponses extends CbroSerialization
object EtapasProcesalesResponses {

  case class GetEtapasProcesalesResponse(registro: Option[EtapasProcesalesTri] = None,
                                         fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
