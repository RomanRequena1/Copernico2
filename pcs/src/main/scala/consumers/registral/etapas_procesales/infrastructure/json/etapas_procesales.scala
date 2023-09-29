package consumers.registral.etapas_procesales.infrastructure

import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesCommands.EtapasProcesalesUpdateFromDto
import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesResponses.GetEtapasProcesalesResponse
import consumers.registral.etapas_procesales.application.entities.{EtapasProcesalesAnt, EtapasProcesalesTri}
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

package object json {

  //COMMANDS
  implicit val EtapasProcesalesUpdateFromDtoDecoder: Decoder[EtapasProcesalesUpdateFromDto] = deriveDecoder
  implicit val EtapasProcesalesUpdateFromDtoEncoder: Encoder[EtapasProcesalesUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val EtapasProcesalesTriDecoder: Decoder[EtapasProcesalesTri] = deriveDecoder
  implicit val EtapasProcesalesTriEncoder: Encoder[EtapasProcesalesTri] = deriveEncoder

  implicit val EtapasProcesalesAntDecoder: Decoder[EtapasProcesalesAnt] = deriveDecoder
  implicit val EtapasProcesalesAntEncoder: Encoder[EtapasProcesalesAnt] = deriveEncoder

  //RESPONSES
  implicit val GetEtapasProcesalesResponseDecoder: Decoder[GetEtapasProcesalesResponse] = deriveDecoder
  implicit val GetEtapasProcesalesResponseEncoder: Encoder[GetEtapasProcesalesResponse] = deriveEncoder

  //EVENTS
  implicit val EtapasProcesalesUpdatedFromDtoDecoder: Decoder[EtapasProcesalesUpdatedFromDto] = deriveDecoder
  implicit val EtapasProcesalesUpdatedFromDtoEncoder: Encoder[EtapasProcesalesUpdatedFromDto] = deriveEncoder

}
