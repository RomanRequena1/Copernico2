package consumers.no_registral.tranferencia.infrastructure.json
import consumers.no_registral.tranferencia.application.entity.TransferenciaResponses.GetTransferenciaResponse
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try
object TranferenciasImplicits {



  //RESPONSES
  implicit val GetTransferenciaResponseDecoder: Decoder[GetTransferenciaResponse] = deriveDecoder
  implicit val GetTransferenciaResponseEncoder: Encoder[GetTransferenciaResponse] = deriveEncoder




}