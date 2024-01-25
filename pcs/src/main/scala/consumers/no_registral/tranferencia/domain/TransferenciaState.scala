package consumers.no_registral.tranferencia.domain

import ddd.AbstractState

import java.time.LocalDateTime


final case class TransferenciaState(
    objetoId: String = "",
    tipoObj: String = "",
    sujetoIdActual: Option[VinculoActual] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN,
    eventCounter: Int = 0,
    map: Map[VinculoActual, Boolean] = Map.empty,
    tiene30ObjetoTranf: Boolean = false
                                   ) extends AbstractState[TransferenciaEvent]{


  def +(event: TransferenciaEvent): TransferenciaState  = ???
}



