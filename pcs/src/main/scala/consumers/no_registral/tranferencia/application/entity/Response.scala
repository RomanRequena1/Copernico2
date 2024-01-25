package consumers.no_registral.tranferencia.application.entity

sealed trait Response

object Response{

  case class ResponseAltaNewVinculo (
                                      sujetoId: String,
                                      objetoId: String,
                                      tipoObj: String,
                                      tiene30ObjetoTranf: String
                                    ) extends Response
}

