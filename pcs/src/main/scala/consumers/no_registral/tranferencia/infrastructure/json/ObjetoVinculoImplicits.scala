package consumers.no_registral.tranferencia.infrastructure.json
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent.ObjetoVinculoSnapshotPersisted
import consumers.no_registral.tranferencia.domain.{Vinculo, VinculoCotitular}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe._
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
      } yield ObjetoVinculoSnapshotPersisted(objetoId, tipoObj, tiene30ObjetoVinculo, mapTransf, mapVinculo)
    }
  }
}



object test extends App{


  implicit val Vinculo1Cotitular1Decoder: Decoder[Vinculo1Cotitular1] = deriveDecoder
  implicit val Vinculo1Cotitular1Encoder: Encoder[Vinculo1Cotitular1] = deriveEncoder

  implicit val Vinculo1Decoder: Decoder[Vinculo1] = deriveDecoder
  implicit val Vinculo1Encoder: Encoder[Vinculo1] = deriveEncoder

  implicit val ObjetoVinculo1SnapshotPersistedEncoder: Encoder[ObjetoVinculo1SnapshotPersisted] = deriveEncoder



  /**
   * Este implicit es para poder serializar un Map[Vinculo1, Vinculo1Cotitular1] a Json
   */

  implicit val mapVinculo1Cotitular1Encoder: Encoder[Map[Vinculo1, Vinculo1Cotitular1]] = new Encoder[Map[Vinculo1, Vinculo1Cotitular1]] {
    override def apply(map: Map[Vinculo1, Vinculo1Cotitular1]): Json = {
      Json.fromFields(map.map { case (k, v) =>
        k.asJson.toString -> v.asJson
      })
    }
  }

  /**
   * Este implicit es para poder serializar un Map[Vinculo1, Vinculo1Cotitular1] a Json
   */

  // KeyDecoder for Vinculo1
  implicit val vinculoKeyDecoder: KeyDecoder[Vinculo1] = new KeyDecoder[Vinculo1] {
    override def apply(key: String): Option[Vinculo1] = {
      val c: HCursor = io.circe.parser.parse(key).getOrElse(Json.Null).hcursor
      for {
        sujetoId <- c.downField("sujetoId").as[String].toOption
        objetoId <- c.downField("objetoId").as[String].toOption
        tipoObj <- c.downField("tipoObj").as[String].toOption
      } yield Vinculo1(sujetoId, objetoId, tipoObj)
    }
  }
  // Decoder for ObjetoVinculo1SnapshotPersisted
  implicit val objetoVinculo1Decoder: Decoder[ObjetoVinculo1SnapshotPersisted] = new Decoder[ObjetoVinculo1SnapshotPersisted] {
    override def apply(c: HCursor): Decoder.Result[ObjetoVinculo1SnapshotPersisted] = {
      for {
        objetoId <- c.downField("objetoId").as[String]
        tipoObj <- c.downField("tipoObj").as[String]
        mapTransf <- c.downField("mapTransf").as[Map[Vinculo1, Vinculo1Cotitular1]](Decoder.decodeMap[Vinculo1, Vinculo1Cotitular1])
        mapVinculo1 <- c.downField("mapVinculo1").as[Map[Vinculo1, Vinculo1Cotitular1]](Decoder.decodeMap[Vinculo1, Vinculo1Cotitular1])
      } yield ObjetoVinculo1SnapshotPersisted(objetoId, tipoObj, mapTransf, mapVinculo1)
    }
  }
  final case class Vinculo1Cotitular1(tiene30Objeto: Boolean, isResponsable: Option[Boolean], titularidad: Option[String], estado: Option[String])
  final case class Vinculo1(sujetoId: String, objetoId: String, tipoObj: String)

  case class ObjetoVinculo1SnapshotPersisted(
                                              objetoId: String,
                                              tipoObj: String,
                                              mapTransf: Map[Vinculo1, Vinculo1Cotitular1],
                                              mapVinculo1: Map[Vinculo1, Vinculo1Cotitular1]
                                            )

  val test  = ObjetoVinculo1SnapshotPersisted("objetoId11",
    "tipoObj11",
    Map(Vinculo1("sujetoId11", "objetoId11", "tipoObj11") -> Vinculo1Cotitular1(true, Some(true), Some("titularidad"), Some("estado"))),
    Map(Vinculo1("sujetoId22", "objetoId22", "tipoObj22") -> Vinculo1Cotitular1(true, Some(true), Some("titularidad"), Some("estado"))))

  val json = test.asJson
  println(json)
  import io.circe.parser.decode
  val d = decode[ObjetoVinculo1SnapshotPersisted]("{\"objetoId\":\"AUTO1\",\"tipoObj\":\"A\",\"mapTransf\":{},\"mapVinculo1\":{\"{\\n  \\\"sujetoId\\\" : \\\"00020092018135\\\",\\n  \\\"objetoId\\\" : \\\"AUTO1\\\",\\n  \\\"tipoObj\\\" : \\\"A\\\"\\n}\":{\"tiene30Objeto\":true,\"isResponsable\":true,\"titularidad\":\"CONDOMINIO\",\"estado\":null}}}")
  val decoded = json.as[ObjetoVinculo1SnapshotPersisted]

  println(decoded)
println("---------------")
  println(d)
}