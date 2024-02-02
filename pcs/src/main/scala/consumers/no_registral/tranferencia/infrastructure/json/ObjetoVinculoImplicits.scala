package consumers.no_registral.tranferencia.infrastructure.json
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent.ObjetoVinculoSnapshotPersisted
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
object ObjetoVinculoImplicits {



  //RESPONSES
  implicit val GetObjetoVinculoResponseDecoder: Decoder[GetObjetoVinculoResponse] = deriveDecoder
  implicit val GetObjetoVinculoResponseEncoder: Encoder[GetObjetoVinculoResponse] = deriveEncoder

  //EVENTS
  implicit val ObjetoVinculoSnapshotPersistedDecoder: Decoder[ObjetoVinculoSnapshotPersisted] = deriveDecoder
  implicit val ObjetoVinculoSnapshotPersistedEncoder: Encoder[ObjetoVinculoSnapshotPersisted] = deriveEncoder



}