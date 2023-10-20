package consumers.registral.contacto.domain

import serialization.CbroSerialization

case class ContactoExternalDto(CNC_ID: String) extends ddd.ExternalDto with CbroSerialization
