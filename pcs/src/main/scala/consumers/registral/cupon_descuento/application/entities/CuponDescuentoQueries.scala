package consumers.registral.cupon_descuento.application.entities

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoResponses.GetCuponDescuentoResponse
import design_principles.actor_model.Query

sealed trait CuponDescuentoQueries extends Query with CuponDescuentoMessage


object CuponDescuentoQueries {
  case class GetStateCuponDescuento(
                             sujetoId: String,
                             objetoId: String,
                             tipoObjeto: String,
                             obligacionId: String
                                   ) extends CuponDescuentoQueries {
    override type ReturnType = GetCuponDescuentoResponse


  }
}
