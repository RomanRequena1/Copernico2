package proyectionists.no_registrales.testkit

import java.time.LocalDateTime
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{
  DetallesObjeto,
  ListDetallesObjeto,
  ObjetosTri
}
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.obligacion.application.entities.{
  DetallesObligacion,
  DetallesSupresiones,
  ListDetallesObligaciones,
  ListDetallesSupresiones,
  ObligacionesAnt,
  ObligacionesTri
}
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import io.circe.syntax.EncoderOps
import utils.generators.Model.{deliveryId, deliveryIdAct}

class Examples(testName: String) {
  val sujetoId1 = s"${testName}Sujeto1"
  val sujetoId2 = s"${testName}Sujeto2"

  val objetoId2: (String, String) = ("2", "I")
//  val objeto2: ObjetosTri = objetoTriStub.copy(
//    EV_ID = deliveryIdAct,
//    SOJ_SUJ_IDENTIFICADOR = sujetoId1,
//    SOJ_IDENTIFICADOR = objetoId2._1,
//    SOJ_TIPO_OBJETO = objetoId2._2
//  )

  val detallesObligacionPago = Some(
    ListDetallesObligaciones(
      List(
        DetallesObligacion(
          EV_ID = Some(deliveryIdAct),
          EVO_OBN_PEO_ID_MATERIAL = None,
          BOB_MUNICIPIO = None,
          BOB_INTERES_FINANCIACION = None,
          JUICIO_MULTIOBJETO = None,
          RULE_NUMBER = Some("1"),
          EVO_OBN_PEO_ID_FORMAL = None,
          PLAN_MULTIOBJETO = None,
          tiene30Obligaciones = None,
          BAND_BATCH = None,
          SOJ_ID_EXTERNO = None
        )
      )
    )
  )

  val otrosAtrib = Some(
    ListDetallesObligaciones(
      List(
        DetallesObligacion(
          BOB_MUNICIPIO = None,
          RULE_NUMBER = Some("1"),
          tiene30Obligaciones = None,
          BAND_BATCH = Some(true),
          EV_ID = None,
          SOJ_ID_EXTERNO = None,
          EVO_OBN_PEO_ID_MATERIAL = None,
          JUICIO_MULTIOBJETO = None,
          BOB_INTERES_FINANCIACION = None,
          EVO_OBN_PEO_ID_FORMAL = None,
          PLAN_MULTIOBJETO = None
        )
      )
    )
  )

  val detallesObligacion: Some[ListDetallesObligaciones] = Some(
    ListDetallesObligaciones(
      List(
        DetallesObligacion(
          EV_ID = Some(deliveryIdAct),
          EVO_OBN_PEO_ID_MATERIAL = None,
          BOB_MUNICIPIO = None,
          BOB_INTERES_FINANCIACION = None,
          JUICIO_MULTIOBJETO = None,
          RULE_NUMBER = Some("1"),
          EVO_OBN_PEO_ID_FORMAL = None,
          PLAN_MULTIOBJETO = None,
          tiene30Obligaciones = None,
          BAND_BATCH = None,
          SOJ_ID_EXTERNO = None
        )
      )
    )
  )

  val detallesObjetoNoResponsable = Some(
    ListDetallesObjeto(
      List(
        DetallesObjeto(
          RESPONSABLE_OTROS_ATRIBUTOS = Some("N"),
          PORCENTAJE_OTROS_ATRIBUTOS = None,
          CUENTA_SOJ_OTROS_ATRIBUTOS = None,
          OTROS_ATRIBUTOS_ADHERIDO_DEBITO = None,
          PERIODO_SOJ_OTROS_ATRIBUTOS = None,
          IMPORTE_SOJ_OTROS_ATRIBUTOS = None,
          SOJ_SEMAFORO_COLOR = None,
          SOJ_SEMAFORO_MARCA = None,
          SOJ_ADQUIRIDO_SUBASTA = None,
          FECHA_SUBASTA = None,
          SOJ_OWNER = None
        )
      )
    )
  )

  val detallesObjetoResponsable = Some(
    ListDetallesObjeto(
      List(
        DetallesObjeto(
          RESPONSABLE_OTROS_ATRIBUTOS = Some("S"),
          PORCENTAJE_OTROS_ATRIBUTOS = None,
          CUENTA_SOJ_OTROS_ATRIBUTOS = None,
          OTROS_ATRIBUTOS_ADHERIDO_DEBITO = None,
          PERIODO_SOJ_OTROS_ATRIBUTOS = None,
          IMPORTE_SOJ_OTROS_ATRIBUTOS = None,
          SOJ_SEMAFORO_COLOR = None,
          SOJ_SEMAFORO_MARCA = None,
          SOJ_ADQUIRIDO_SUBASTA = None,
          FECHA_SUBASTA = None,
          SOJ_OWNER = None
        )
      )
    )
  )

  val objetoExample = ObjetosTri(
    RULE_NUMBER = None,
    EV_ID = deliveryIdAct,
    SOJ_SUJ_IDENTIFICADOR = "20-43271253-3",
    SOJ_TIPO_OBJETO = "A",
    SOJ_IDENTIFICADOR = "ABC123",
    SOJ_CAT_SOJ_ID = None,
    SOJ_DESCRIPCION = Some("Auto"),
    SOJ_ESTADO = None,
    SOJ_FECHA_INICIO = None,
    SOJ_FECHA_FIN = None,
    SOJ_ID_EXTERNO = Some("1234"),
    SOJ_OTROS_ATRIBUTOS = detallesObjetoNoResponsable,
    SOJ_BASE_IMPONIBLE = None,
    SOJ_ADHERIDO_DEBITO = Some("N"),
    SOJ_CANT_CUOTAS_PAGADAS = None,
    SOJ_CANAL_ORIGEN = Some("OTAX"),
    SOJ_SUBTIPO = None,
    SOJ_IDENTIFICADOR_2 = None,
    SOJ_TITULARIDAD = Some("CONDOMINIO"),
    SOJ_TIPO_EXCLUSION = None,
    SOJ_FECHA_VTA_SUBASTA = None,
    SOJ_FECHA_ADQ_SUBASTA = None
  )

  val objetoReadside = ObjetoSnapshotPersisted(
    deliveryId = 1234,
    sujetoId = "String",
    objetoId = "String",
    objetoId2 = None,
    tipoObjeto = "String",
    saldo = 500.50,
    cotitulares = Set(),
    tags = Set(),
    sujetoResponsable = None,
    porcentajeResponsabilidad = 99.9,
    registro = Some(objetoExample),
    obligacionesSaldo = Map.empty,
    cuotas = List(true),
    bandTipo = "1",
    operacion = "U",
    idExterno = None,
    tiene30Objeto = Some(true),
    aplicarDescuento = Some(true),
    resultDmn = 1,
    exclusionObjeto = "",
    tiene30ObjetoVinculo = Some(true)
  )

  val obligacionExampleVencida = ObligacionesTri(
    RULE_NUMBER = None,
    EV_ID = deliveryIdAct,
    BOB_SUJ_IDENTIFICADOR = "20-45678910-2",
    BOB_SOJ_TIPO_OBJETO = "A",
    BOB_SOJ_IDENTIFICADOR = "ABC123",
    BOB_OBN_ID = "None",
    BOB_ESTADO = Some("ADMINISTRATIVA"),
    BOB_PRORROGA = Some(LocalDateTime.of(2021, 12, 12, 0, 0)),
    BOB_VENCIMIENTO = Some(LocalDateTime.of(2021, 12, 12, 0, 0)),
    BOB_VENCIMIENTO_2 = Some(LocalDateTime.of(2021, 12, 12, 0, 0)),
    BOB_CAPITAL = None,
    BOB_CONCEPTO = None,
    BOB_IMPUESTO = None,
    BOB_OTROS_ATRIBUTOS = detallesObligacion,
    BOB_SALDO = 200,
    BOB_SOJ_IDENTIFICADOR_2 = None,
    BOB_ADHERIDO_DEBITO = None,
    BOB_CANAL_ORIGEN = None,
    BOB_TPBID = None,
    BOB_CUOTA = None,
    BOB_FECHASANCION = None,
    BOB_FISCALIZADA = None,
    BOB_SUB_ESTADO = None,
    BOB_INDICE_INT_PUNIT = None,
    BOB_INDICE_INT_RESAR = None,
    BOB_INTERES_PUNIT = None,
    BOB_INTERES_RESAR = None,
    BOB_JUI_ID = None,
    BOB_PERIODO = Some("2024"),
    BOB_PLN_ID = None,
    BOB_TIPO = None,
    BOB_TOTAL = None,
    BOB_OGA_ID = None,
    SOJ_ID_EXTERNO = None,
    BOB_SUPRESIONES = None
  )

  val detallesSupresiones =
    Some(
      ListDetallesSupresiones(
        List(
          DetallesSupresiones(
            BOB_TIPO_SUP = Some("Tipo Supresion"),
            BOB_ESTADO_SUP = Some("Estado Supresion"),
            BOB_FECHA_INICIO_SUP = Some(LocalDateTime.of(2023, 8, 14, 0, 0)),
            BOB_FECHA_FIN_SUP = Some(LocalDateTime.of(2025, 8, 14, 0, 0))
          )
        )
      )
    )

  val obligacionExampleAntVencida = ObligacionesAnt(
    RULE_NUMBER = None,
    EV_ID = deliveryIdAct,
    BOB_SUJ_IDENTIFICADOR = "Diego",
    BOB_SOJ_TIPO_OBJETO = "A",
    BOB_SOJ_IDENTIFICADOR = "AutoDiego",
    BOB_OBN_ID = "2001",
    BOB_ESTADO = Some("ADMINISTRATIVA"),
    BOB_PRORROGA = None,
    BOB_VENCIMIENTO = Some(LocalDateTime.of(2023, 8, 14, 0, 0)),
    BOB_VENCIMIENTO_2 = None,
    BOB_CAPITAL = Some(1000),
    BOB_CONCEPTO = Some("601"),
    BOB_IMPUESTO = Some("600"),
    BOB_OTROS_ATRIBUTOS = detallesObligacion,
    BOB_SALDO = 1000,
    BOB_SOJ_IDENTIFICADOR_2 = None,
    BOB_ADHERIDO_DEBITO = None,
    BOB_CANAL_ORIGEN = Some("LOCAL"),
    BOB_TPBID = None,
    BOB_CUOTA = Some("1"),
    BOB_FECHASANCION = None,
    BOB_FISCALIZADA = None,
    BOB_SUB_ESTADO = None,
    BOB_INDICE_INT_PUNIT = None,
    BOB_INDICE_INT_RESAR = None,
    BOB_INTERES_PUNIT = None,
    BOB_INTERES_RESAR = None,
    BOB_JUI_ID = None,
    BOB_PERIODO = Some("2024"),
    BOB_PLN_ID = None,
    BOB_TIPO = Some("ANT"),
    BOB_TOTAL = None,
    BOB_OGA_ID = None,
    SOJ_ID_EXTERNO = None,
    BOB_SUPRESIONES = detallesSupresiones
  )

  val obligacionExampleAntProjection =
    ObligacionPersistedSnapshot(
      deliveryId = deliveryIdAct,
      sujetoId = obligacionExampleAntVencida.BOB_SUJ_IDENTIFICADOR,
      objetoId = obligacionExampleAntVencida.BOB_SOJ_IDENTIFICADOR,
      tipoObjeto = obligacionExampleAntVencida.BOB_SOJ_TIPO_OBJETO,
      obligacionId = obligacionExampleAntVencida.BOB_OBN_ID,
      registro = Some(obligacionExampleAntVencida),
      exenta = true,
      porcentajeExencion = 0,
      saldo = obligacionExampleAntVencida.BOB_SALDO,
      operacion = ObligacionEvents.operaciones("Upsert"),
      resultDmn = Some("-1")
    )

  val obligacionExamplePaga = ObligacionesTri(
    RULE_NUMBER = None,
    EV_ID = deliveryIdAct,
    BOB_SUJ_IDENTIFICADOR = "20-45678910-2",
    BOB_SOJ_TIPO_OBJETO = "A",
    BOB_SOJ_IDENTIFICADOR = "ABC123",
    BOB_OBN_ID = "None",
    BOB_ESTADO = Some("ADMINISTRATIVA"),
    BOB_PRORROGA = Some(LocalDateTime.of(2021, 12, 12, 0, 0)),
    BOB_VENCIMIENTO = Some(LocalDateTime.of(2021, 12, 12, 0, 0)),
    BOB_VENCIMIENTO_2 = Some(LocalDateTime.of(2021, 12, 12, 0, 0)),
    BOB_CAPITAL = None,
    BOB_CONCEPTO = None,
    BOB_IMPUESTO = None,
    BOB_OTROS_ATRIBUTOS = detallesObligacionPago,
    BOB_SALDO = 100.00,
    BOB_SOJ_IDENTIFICADOR_2 = None,
    BOB_ADHERIDO_DEBITO = None,
    BOB_CANAL_ORIGEN = None,
    BOB_TPBID = None,
    BOB_CUOTA = None,
    BOB_FECHASANCION = None,
    BOB_FISCALIZADA = None,
    BOB_SUB_ESTADO = None,
    BOB_INDICE_INT_PUNIT = None,
    BOB_INDICE_INT_RESAR = None,
    BOB_INTERES_PUNIT = None,
    BOB_INTERES_RESAR = None,
    BOB_JUI_ID = None,
    BOB_PERIODO = None,
    BOB_PLN_ID = None,
    BOB_TIPO = None,
    BOB_TOTAL = None,
    BOB_OGA_ID = None,
    SOJ_ID_EXTERNO = None,
    BOB_SUPRESIONES = None
  )

  val objetoExampleConExclusion = ObjetosTri(
    RULE_NUMBER = None,
    EV_ID = deliveryIdAct,
    SOJ_SUJ_IDENTIFICADOR = "20-43271253-3",
    SOJ_TIPO_OBJETO = "A",
    SOJ_IDENTIFICADOR = "ABC123",
    SOJ_CAT_SOJ_ID = None,
    SOJ_DESCRIPCION = Some("Auto"),
    SOJ_ESTADO = None,
    SOJ_FECHA_INICIO = None,
    SOJ_FECHA_FIN = None,
    SOJ_ID_EXTERNO = Some("1234"),
    SOJ_OTROS_ATRIBUTOS = detallesObjetoNoResponsable,
    SOJ_BASE_IMPONIBLE = None,
    SOJ_ADHERIDO_DEBITO = Some("N"),
    SOJ_CANT_CUOTAS_PAGADAS = None,
    SOJ_CANAL_ORIGEN = Some("OTAX"),
    SOJ_SUBTIPO = None,
    SOJ_IDENTIFICADOR_2 = None,
    SOJ_TITULARIDAD = Some("CONDOMINIO"),
    SOJ_TIPO_EXCLUSION = Some("E"),
    SOJ_FECHA_VTA_SUBASTA = None,
    SOJ_FECHA_ADQ_SUBASTA = None
  )

  val objetoExampleConExclusionVencida = ObjetosTri(
    RULE_NUMBER = None,
    EV_ID = deliveryIdAct,
    SOJ_SUJ_IDENTIFICADOR = "20-43271253-3",
    SOJ_TIPO_OBJETO = "A",
    SOJ_IDENTIFICADOR = "ABC123",
    SOJ_CAT_SOJ_ID = None,
    SOJ_DESCRIPCION = Some("Auto"),
    SOJ_ESTADO = None,
    SOJ_FECHA_INICIO = None,
    SOJ_FECHA_FIN = None,
    SOJ_ID_EXTERNO = Some("1234"),
    SOJ_OTROS_ATRIBUTOS = detallesObjetoNoResponsable,
    SOJ_BASE_IMPONIBLE = None,
    SOJ_ADHERIDO_DEBITO = Some("N"),
    SOJ_CANT_CUOTAS_PAGADAS = None,
    SOJ_CANAL_ORIGEN = Some("OTAX"),
    SOJ_SUBTIPO = None,
    SOJ_IDENTIFICADOR_2 = None,
    SOJ_TITULARIDAD = Some("CONDOMINIO"),
    SOJ_TIPO_EXCLUSION = Some("NE"),
    SOJ_FECHA_VTA_SUBASTA = None,
    SOJ_FECHA_ADQ_SUBASTA = None
  )

  def objetoExampleLucas = objetoExample.copy(EV_ID = deliveryIdAct, SOJ_SUJ_IDENTIFICADOR = "20-43271253-3")
  def objetoExampleRoman = objetoExample.copy(EV_ID = deliveryIdAct, SOJ_SUJ_IDENTIFICADOR = "20-45678910-2")
  def objetoExampleDiego = objetoExample.copy(EV_ID = deliveryIdAct, SOJ_SUJ_IDENTIFICADOR = "20-40123456-1")

  def obligacionExampleVencidaLucas =
    obligacionExampleVencida.copy(EV_ID = deliveryIdAct, BOB_SUJ_IDENTIFICADOR = "20-43271253-3")
  def obligacionExampleVencidaRoman =
    obligacionExampleVencida.copy(EV_ID = deliveryIdAct, BOB_SUJ_IDENTIFICADOR = "20-45678910-2")
  def obligacionExampleVencidaDiego =
    obligacionExampleVencida.copy(EV_ID = deliveryIdAct, BOB_SUJ_IDENTIFICADOR = "20-40123456-1")

  def ObjetoExampleConExclusionRoman =
    objetoExampleConExclusion.copy(EV_ID = deliveryIdAct, SOJ_SUJ_IDENTIFICADOR = "20-45678910-2")
  def ObjetoExampleConExclusionDiego =
    objetoExampleConExclusion.copy(EV_ID = deliveryIdAct, SOJ_SUJ_IDENTIFICADOR = "20-40123456-1")

  def ObjetoExampleConExclusionVencidaRoman =
    objetoExampleConExclusionVencida.copy(EV_ID = deliveryIdAct, SOJ_SUJ_IDENTIFICADOR = "20-45678910-2")

  def obligacionExamplePagaLucas =
    obligacionExamplePaga.copy(EV_ID = deliveryIdAct, BOB_SUJ_IDENTIFICADOR = "20-43271253-3")

  def objetoResponsable(objetoExample: ObjetosTri) = {
    objetoExample.copy(SOJ_OTROS_ATRIBUTOS = detallesObjetoResponsable)
  }

  val fechaVencimientoObligacion5: LocalDateTime = LocalDateTime.now.plusMinutes(5)
  val obligacionId = "1"
  /*  private def obligacionExample2: ObligacionesTri =
    stubs.consumers.no_registrales.obligacion.ObligacionExternalDtoStub.obligacionesTri.copy(
      EV_ID = deliveryIdAct,
      BOB_SUJ_IDENTIFICADOR = sujetoId1,
      BOB_SOJ_IDENTIFICADOR = objetoId2._1,
      BOB_SOJ_TIPO_OBJETO = objetoId2._2,
      BOB_OBN_ID = obligacionId,
      BOB_SALDO = 70,
      BOB_VENCIMIENTO = Some(fechaVencimientoObligacion5)
    )*/
  private def obligacionExample: ObligacionesTri = {
    val otrosAtrib = Some(
      ListDetallesObligaciones(
        List(
          DetallesObligacion(
            BOB_MUNICIPIO = None,
            RULE_NUMBER = Some("1"),
            tiene30Obligaciones = None,
            BAND_BATCH = Some(true),
            EV_ID = None,
            SOJ_ID_EXTERNO = None,
            EVO_OBN_PEO_ID_MATERIAL = None,
            JUICIO_MULTIOBJETO = None,
            BOB_INTERES_FINANCIACION = None,
            EVO_OBN_PEO_ID_FORMAL = None,
            PLAN_MULTIOBJETO = None
          )
        )
      )
    )

    val obnTest = ObligacionesTri(
      BOB_SALDO = 100.00,
      BOB_SUJ_IDENTIFICADOR = "20-43271253-3",
      BOB_SOJ_TIPO_OBJETO = "A",
      BOB_SOJ_IDENTIFICADOR = "ABC123",
      BOB_OBN_ID = "1",
      BOB_SOJ_IDENTIFICADOR_2 = Some("20-43271253-3"),
      BOB_ADHERIDO_DEBITO = Some("N"),
      BOB_CANAL_ORIGEN = Some("OTAX"),
      BOB_TPBID = None,
      BOB_CAPITAL = Some(1.00),
      BOB_CUOTA = Some("1"),
      BOB_ESTADO = None,
      BOB_CONCEPTO = Some("AUTO"),
      BOB_FECHASANCION = None,
      BOB_FISCALIZADA = None,
      BOB_IMPUESTO = None,
      BOB_SUB_ESTADO = None,
      BOB_INDICE_INT_PUNIT = None,
      BOB_INDICE_INT_RESAR = None,
      BOB_INTERES_PUNIT = None,
      BOB_INTERES_RESAR = None,
      BOB_JUI_ID = None,
      BOB_OTROS_ATRIBUTOS = otrosAtrib,
      BOB_PERIODO = Some("2024"),
      BOB_PLN_ID = None,
      BOB_PRORROGA = Some(LocalDateTime.of(2024, 12, 12, 0, 0)),
      BOB_TIPO = Some("TIPO"),
      BOB_TOTAL = Some(100.0),
      BOB_VENCIMIENTO = Some(LocalDateTime.of(2024, 12, 12, 0, 0)),
      BOB_VENCIMIENTO_2 = None,
      BOB_OGA_ID = None,
      EV_ID = deliveryIdAct,
      RULE_NUMBER = None,
      SOJ_ID_EXTERNO = Some("1234"),
      BOB_SUPRESIONES = None
    )
    obnTest
  }

  def obligacionWithSaldo200 =
    obligacionExample
      .copy(BOB_SALDO = 200)
      .copy(EV_ID = deliveryIdAct)
  def obligacionWithSaldo50 =
    obligacionWithSaldo200
      .copy(BOB_SALDO = 50)
      .copy(EV_ID = deliveryIdAct)
  def obligacionVencida =
    obligacionWithSaldo50
      .copy(BOB_VENCIMIENTO = Some(LocalDateTime.now.minusDays(1)))
      .copy(EV_ID = deliveryIdAct)
  val juicioId = 1
  def obligacionWithJuicio =
    obligacionWithSaldo50
      .copy(BOB_JUI_ID = Some(juicioId))
      .copy(EV_ID = deliveryIdAct)

  private def obligacionAntExample: ObligacionesAnt = {
    val otrosAtrib = Some(
      ListDetallesObligaciones(
        List(
          DetallesObligacion(
            BOB_MUNICIPIO = None,
            RULE_NUMBER = Some("1"),
            tiene30Obligaciones = None,
            BAND_BATCH = Some(true),
            EV_ID = None,
            SOJ_ID_EXTERNO = None,
            EVO_OBN_PEO_ID_MATERIAL = None,
            JUICIO_MULTIOBJETO = None,
            BOB_INTERES_FINANCIACION = None,
            EVO_OBN_PEO_ID_FORMAL = None,
            PLAN_MULTIOBJETO = None
          )
        )
      )
    )

    val supresiones = Some(
      ListDetallesSupresiones(
        List(
          DetallesSupresiones(
            BOB_TIPO_SUP = Some("Tipo Supresion"),
            BOB_ESTADO_SUP = Some("Estado Supresion"),
            BOB_FECHA_INICIO_SUP = Some(LocalDateTime.of(2023, 12, 12, 0, 0)),
            BOB_FECHA_FIN_SUP = Some(LocalDateTime.of(2025, 12, 12, 0, 0))
          )
        )
      )
    )

    val obnAntTest = ObligacionesAnt(
      BOB_SALDO = 1000,
      BOB_SUJ_IDENTIFICADOR = "Diego",
      BOB_SOJ_TIPO_OBJETO = "A",
      BOB_SOJ_IDENTIFICADOR = "AutoDiego",
      BOB_OBN_ID = "2001",
      BOB_SOJ_IDENTIFICADOR_2 = None,
      BOB_ADHERIDO_DEBITO = Some("N"),
      BOB_CANAL_ORIGEN = Some("LOCAL"),
      BOB_TPBID = None,
      BOB_CAPITAL = Some(1000),
      BOB_CUOTA = Some("1"),
      BOB_ESTADO = Some("ADMINISTRATIVA"),
      BOB_CONCEPTO = Some("601"),
      BOB_FECHASANCION = None,
      BOB_FISCALIZADA = Some("N"),
      BOB_IMPUESTO = Some("600"),
      BOB_SUB_ESTADO = None,
      BOB_INDICE_INT_PUNIT = None,
      BOB_INDICE_INT_RESAR = None,
      BOB_INTERES_PUNIT = None,
      BOB_INTERES_RESAR = None,
      BOB_JUI_ID = None,
      BOB_OTROS_ATRIBUTOS = otrosAtrib,
      BOB_PERIODO = Some("2024"),
      BOB_PLN_ID = None,
      BOB_PRORROGA = Some(LocalDateTime.of(2024, 12, 12, 0, 0)),
      BOB_TIPO = Some("ANT"),
      BOB_TOTAL = Some(100.0),
      BOB_VENCIMIENTO = Some(LocalDateTime.of(2024, 12, 12, 0, 0)),
      BOB_VENCIMIENTO_2 = None,
      BOB_OGA_ID = None,
      EV_ID = deliveryIdAct,
      RULE_NUMBER = None,
      SOJ_ID_EXTERNO = Some("1234"),
      BOB_SUPRESIONES = supresiones
    )
    obnAntTest
  }

  def obligacionAntWithSaldo200 =
    obligacionAntExample
      .copy(BOB_SALDO = 200)
      .copy(EV_ID = deliveryIdAct)
  def obligacionAntWithSaldo50 =
    obligacionAntWithSaldo200
      .copy(BOB_SALDO = 50)
      .copy(EV_ID = deliveryIdAct)
  def obligacionAntVencida =
    obligacionAntWithSaldo50
      .copy(BOB_VENCIMIENTO = Some(LocalDateTime.now.minusDays(1)))
      .copy(EV_ID = deliveryIdAct)
  val juicioAntId = 1
  def obligacionAntWithJuicio =
    obligacionAntWithSaldo50
      .copy(BOB_JUI_ID = Some(juicioAntId))
      .copy(EV_ID = deliveryIdAct)

}
