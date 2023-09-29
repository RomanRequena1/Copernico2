package consumers.no_registral.sujeto.infrastructure.json

import consumers.no_registral.sujeto.application.entity.{SujetoAnt, SujetoTri}
import consumers.no_registral.sujeto.application.entity.SujetoCommands.{SujetoSetBajaFromObjeto, SujetoUpdateFromAnt, SujetoUpdateFromObjeto, SujetoUpdateFromTri}
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers.no_registral.sujeto.domain.SujetoEvents.{SujetoBajaFromObjetoSet, SujetoSnapshotPersisted, SujetoUpdatedFromAnt, SujetoUpdatedFromObjeto, SujetoUpdatedFromTri}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


object SujetosImplicits {
  //COMMANDS

  implicit val SujetoUpdateFromTriDecoder: Decoder[SujetoUpdateFromTri] = deriveDecoder
  implicit val SujetoUpdateFromTriEncoder: Encoder[SujetoUpdateFromTri] = deriveEncoder


  implicit val SujetoUpdateFromAntDecoder: Decoder[SujetoUpdateFromAnt] = deriveDecoder
  implicit val SujetoUpdateFromAntEncoder: Encoder[SujetoUpdateFromAnt] = deriveEncoder


  implicit val SujetoUpdateFromObjetoDecoder: Decoder[SujetoUpdateFromObjeto] = deriveDecoder
  implicit val SujetoUpdateFromObjetoEncoder: Encoder[SujetoUpdateFromObjeto] = deriveEncoder

  implicit val SujetoSetBajaFromObjetoDecoder: Decoder[SujetoSetBajaFromObjeto] = deriveDecoder
  implicit val SujetoSetBajaFromObjetoEncoder: Encoder[SujetoSetBajaFromObjeto] = deriveEncoder

  //EXTERNALDTO


  implicit val SujetoAntDecoder: Decoder[SujetoAnt] = deriveDecoder
  implicit val SujetoAntEncoder: Encoder[SujetoAnt] = deriveEncoder

  implicit val SujetoTriDecoder: Decoder[SujetoTri] = deriveDecoder
  implicit val SujetoTriEncoder: Encoder[SujetoTri] = deriveEncoder

  implicit val GetSujetoResponseDecoder: Decoder[GetSujetoResponse] = deriveDecoder
  implicit val GetSujetoResponseEncoder: Encoder[GetSujetoResponse] = deriveEncoder

  //EVENTS

  implicit val SujetoSnapshotPersistedDecoder: Decoder[SujetoSnapshotPersisted] = deriveDecoder
  implicit val SujetoSnapshotPersistedEncoder: Encoder[SujetoSnapshotPersisted] = deriveEncoder


  implicit val SujetoUpdatedFromTriDecoder: Decoder[SujetoUpdatedFromTri] = deriveDecoder
  implicit val SujetoUpdatedFromTriEncoder: Encoder[SujetoUpdatedFromTri] = deriveEncoder


  implicit val SujetoUpdatedFromAntDecoder: Decoder[SujetoUpdatedFromAnt] = deriveDecoder
  implicit val SujetoUpdatedFromAntEncoder: Encoder[SujetoUpdatedFromAnt] = deriveEncoder


  implicit val SujetoUpdatedFromObjetoDecoder: Decoder[SujetoUpdatedFromObjeto] = deriveDecoder
  implicit val SujetoUpdatedFromObjetoEncoder: Encoder[SujetoUpdatedFromObjeto] = deriveEncoder

  implicit val SujetoBajaFromObjetoSetDecoder: Decoder[SujetoBajaFromObjetoSet] = deriveDecoder
  implicit val SujetoBajaFromObjetoSetEncoder: Encoder[SujetoBajaFromObjetoSet] = deriveEncoder

}
