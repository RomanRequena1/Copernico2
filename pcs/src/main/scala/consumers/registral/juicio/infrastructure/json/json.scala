package consumers.registral.juicio.infrastructure

import consumers.registral.juicio.application.entities.JuicioCommands.JuicioUpdateFromDto
import consumers.registral.juicio.application.entities.JuicioResponses.GetJuicioResponse
import consumers.registral.juicio.application.entities.{DetallesJuicio, JuicioTri}
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

package object json {


  //COMMANDS
  implicit val JuicioUpdateFromDtoDecoder: Decoder[JuicioUpdateFromDto] = deriveDecoder
  implicit val JuicioUpdateFromDtoEncoder: Encoder[JuicioUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val JuicioTriDecoder: Decoder[JuicioTri] = deriveDecoder
  implicit val JuicioTriEncoder: Encoder[JuicioTri] = deriveEncoder

  implicit val DetallesJuicioDecoder: Decoder[DetallesJuicio] = deriveDecoder
  implicit val DetallesJuicioEncoder: Encoder[DetallesJuicio] = deriveEncoder

  //RESPONSES
  implicit val GetJuicioResponseDecoder: Decoder[GetJuicioResponse] = deriveDecoder
  implicit val GetJuicioResponseEncoder: Encoder[GetJuicioResponse] = deriveEncoder

  //EVENTS
  implicit val JuicioUpdatedFromDtoDecoder: Decoder[JuicioUpdatedFromDto] = deriveDecoder
  implicit val JuicioUpdatedFromDtoEncoder: Encoder[JuicioUpdatedFromDto] = deriveEncoder


}
