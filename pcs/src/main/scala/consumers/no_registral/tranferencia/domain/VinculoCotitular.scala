package consumers.no_registral.tranferencia.domain

final case class VinculoCotitular(tiene30Objeto: Boolean, isResponsable: Option[Boolean], titularidad: Option[String], estado: Option[String])

