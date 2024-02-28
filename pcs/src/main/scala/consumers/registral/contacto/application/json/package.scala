package consumers.registral.contacto.application.json

import consumers.registral.contacto.domain.ContactoCommands.ContactoUpdateFromDto
import consumers.registral.contacto.domain.ContactoEvents.ContactoUpdatedFromDto
import consumers.registral.contacto.domain.ContactoExternalDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

 object json {
   implicit val ContactoUpdateFromDtoDecoder: Decoder[ContactoUpdateFromDto] = deriveDecoder
   implicit val ContactoUpdateFromDtoEncoder: Encoder[ContactoUpdateFromDto] = deriveEncoder

   implicit val ContactoExternalDtoDecoder: Decoder[ContactoExternalDto] = deriveDecoder
   implicit val ContactoExternalDtoEncoder: Encoder[ContactoExternalDto] = deriveEncoder

   implicit val ContactoUpdatedFromDtoDecoder: Decoder[ContactoUpdatedFromDto] = deriveDecoder
   implicit val ContactoUpdatedFromDtoEncoder: Encoder[ContactoUpdatedFromDto] = deriveEncoder
}
