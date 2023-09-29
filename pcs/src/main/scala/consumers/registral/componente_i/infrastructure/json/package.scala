package consumers.registral.componente_i.infrastructure

import consumers.registral.componente_i.application.entities.ComponenteICommands.{ComponenteIRemove, ComponenteIUpdateFromDto}
import consumers.registral.componente_i.application.entities.ComponenteIResponses.GetComponenteIResponse
import consumers.registral.componente_i.application.entities.{ComponenteITri, DetallesComponenteI}
import consumers.registral.componente_i.domain.ComponenteIEvents.{ComponenteIPersistedSnapshot, ComponenteIRemoved, ComponenteIUpdatedFromDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
object json {
  implicit val ComponenteIRemoveDecoder: Decoder[ComponenteIRemove] = deriveDecoder
  implicit val ComponenteIRemoveEncoder: Encoder[ComponenteIRemove] = deriveEncoder
  implicit val ComponenteIUpdateFromDtoDecoder: Decoder[ComponenteIUpdateFromDto] = deriveDecoder
  implicit val ComponenteIUpdateFromDtoEncoder: Encoder[ComponenteIUpdateFromDto] = deriveEncoder

  implicit val ComponenteITriDecoder: Decoder[ComponenteITri] = deriveDecoder
  implicit val ComponenteITriEncoder: Encoder[ComponenteITri] = deriveEncoder

  implicit val DetallesComponenteIDecoder: Decoder[DetallesComponenteI] = deriveDecoder
  implicit val DetallesComponenteIEncoder: Encoder[DetallesComponenteI] = deriveEncoder

  implicit val GetComponenteIResponseDecoder: Decoder[GetComponenteIResponse] = deriveDecoder
  implicit val GetComponenteIResponseEncoder: Encoder[GetComponenteIResponse] = deriveEncoder

  implicit val ComponenteIPersistedSnapshotDecoder: Decoder[ComponenteIPersistedSnapshot] = deriveDecoder
  implicit val ComponenteIPersistedSnapshotEncoder: Encoder[ComponenteIPersistedSnapshot] = deriveEncoder


  implicit val ComponenteIUpdatedFromDtoDecoder: Decoder[ComponenteIUpdatedFromDto] = deriveDecoder
  implicit val ComponenteIUpdatedFromDtoEncoder: Encoder[ComponenteIUpdatedFromDto] = deriveEncoder


  implicit val ComponenteIRemovedDecoder: Decoder[ComponenteIRemoved] = deriveDecoder
  implicit val ComponenteIRemovedEncoder: Encoder[ComponenteIRemoved] = deriveEncoder

}
