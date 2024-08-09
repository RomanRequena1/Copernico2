package consumers.no_registral.tranferencia.infrastructure.json
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent.ObjetoVinculoSnapshotPersisted
import consumers.no_registral.tranferencia.domain.{Vinculo, VinculoCotitular}
import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
object ObjetoVinculoImplicits {


  //RESPONSES
  implicit val GetObjetoVinculoResponseDecoder: Decoder[GetObjetoVinculoResponse] = deriveDecoder
  implicit val GetObjetoVinculoResponseEncoder: Encoder[GetObjetoVinculoResponse] = deriveEncoder

  //EVENTS

  implicit val VinculoCotitularDecoder: Decoder[VinculoCotitular] = deriveDecoder
  implicit val VinculoCotitularEncoder: Encoder[VinculoCotitular] = deriveEncoder

  implicit val VinculoDecoder: Decoder[Vinculo] = deriveDecoder
  implicit val VinculoEncoder: Encoder[Vinculo] = deriveEncoder

  implicit val ObjetoVinculoSnapshotPersistedEncoder: Encoder[ObjetoVinculoSnapshotPersisted] = deriveEncoder

  /**
   * Este implicit es para poder serializar un Map[Vinculo, VinculoCotitular] a Json
   */

  implicit val mapVinculoCotitularEncoder: Encoder[Map[Vinculo, VinculoCotitular]] = new Encoder[Map[Vinculo, VinculoCotitular]] {
    override def apply(map: Map[Vinculo, VinculoCotitular]): Json = {
      Json.fromFields(map.map { case (k, v) =>
        k.asJson.toString -> v.asJson
      })
    }
  }

  /**
   * Este implicit es para poder serializar un Map[Vinculo, VinculoCotitular] a Json
   */

  // KeyDecoder for Vinculo
  implicit val vinculoKeyDecoder: KeyDecoder[Vinculo] = new KeyDecoder[Vinculo] {
    override def apply(key: String): Option[Vinculo] = {
      val c: HCursor = io.circe.parser.parse(key).getOrElse(Json.Null).hcursor
      for {
        sujetoId <- c.downField("sujetoId").as[String].toOption
        objetoId <- c.downField("objetoId").as[String].toOption
        tipoObj <- c.downField("tipoObj").as[String].toOption
      } yield Vinculo(sujetoId, objetoId, tipoObj)
    }
  }


  // Decoder for ObjetoVinculoSnapshotPersisted
  implicit val objetoVinculoDecoder: Decoder[ObjetoVinculoSnapshotPersisted] = new Decoder[ObjetoVinculoSnapshotPersisted] {
    override def apply(c: HCursor): Decoder.Result[ObjetoVinculoSnapshotPersisted] = {
      for {
        objetoId <- c.downField("objetoId").as[String]
        tipoObj <- c.downField("tipoObj").as[String]
        tiene30ObjetoVinculo <- c.downField("tiene30ObjetoVinculo").as[Boolean]
        mapTransf <- c.downField("mapTransf").as[Map[Vinculo, VinculoCotitular]](Decoder.decodeMap[Vinculo, VinculoCotitular])
        mapVinculo <- c.downField("mapVinculo").as[Map[Vinculo, VinculoCotitular]](Decoder.decodeMap[Vinculo, VinculoCotitular])
        exclusionObjetoVinculo <- c.downField("exclusionObjetoVinculo").as[String]
      } yield ObjetoVinculoSnapshotPersisted(objetoId, tipoObj, tiene30ObjetoVinculo, mapTransf, mapVinculo, exclusionObjetoVinculo)
    }
  }
}



