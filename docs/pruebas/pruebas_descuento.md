# Informe de Pruebas - Sistema de Descuentos
## Ambiente DESA - 09/10/2025

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Objetivo de las Pruebas](#objetivo-de-las-pruebas)
3. [Casos de Prueba Ejecutados](#casos-de-prueba-ejecutados)
4. [Problemas Identificados](#problemas-identificados)
   - 4.1. [🔴 Críticos - Prioridad Máxima](#-crítico---prioridad-máxima)
   - 4.2. [🟡 Medios - Prioridad Media](#-medio---prioridad-media)
5. [Funcionalidades Validadas](#funcionalidades-validadas)
6. [Conclusiones y Recomendaciones](#conclusiones-y-recomendaciones)
   - 6.1. [Acciones Inmediatas](#acciones-inmediatas-requeridas)
   - 6.2. [Pruebas Adicionales](#pruebas-adicionales-recomendadas)
   - 6.3. [Impacto en Producción](#impacto-en-producción)
   - 6.4. [Recomendación Final](#recomendación-final)
7. [Información Técnica](#información-técnica)

---

## Resumen Ejecutivo

Este informe presenta los resultados de las pruebas integrales realizadas sobre el **sistema de descuentos del 30%** en el ambiente de desarrollo (DESA). Se evaluaron escenarios complejos incluyendo eventos de sujetos, objetos, obligaciones, transferencias, cotitularidades y exclusiones, identificando **problemas críticos que impactan la recaudación** y requieren corrección inmediata antes del despliegue.

### Hallazgos Principales
🔴 **CRÍTICO**: Se identificaron **3 problemas de alta prioridad** que afectan directamente la aplicación correcta del descuento del 30%, incluyendo penalizaciones incorrectas ante obligaciones a vencer y evaluación errónea en transferencias con responsabilidad solidaria.

✅ **POSITIVO**: El **67% de las funcionalidades** opera correctamente, incluyendo persistencia en Cassandra, manejo de exclusiones tipo C, semáforos rojos y eventos de pago.

### Métricas Generales
- **Total de casos de prueba ejecutados**: 12
- **Casos exitosos**: 8 (67%)
- **Casos con problemas críticos**: 3 (25%)
- **Casos con problemas menores**: 1 (8%)
- **Funcionalidades validadas**: 8

---

## Objetivo de las Pruebas

Validar la **funcionalidad integral del sistema de descuentos del 30%** antes del despliegue en producción, con énfasis en:

### Aspectos Técnicos Evaluados
- **Persistencia de datos**: Correcta inserción y actualización en Cassandra
- **Motor de reglas**: Evaluación del DMN para diferentes tipos de condonación
- **Gestión de marcas**: Comportamiento de `soj_aplicardescuento` y `soj_tiene30objeto`
- **Eventos de integración**: Envío correcto al tópico `dgr-cop-objeto-beneficios-v1`
- **Actor de vínculos**: Manejo de relaciones entre sujetos y objetos

### Escenarios de Negocio Validados
- **Ciclo de vida de obligaciones**: A vencer, vencidas, pagadas
- **Transferencias patrimoniales**: Cambio de titularidad con deuda existente
- **Cotitularidades**: Responsabilidad solidaria entre múltiples sujetos
- **Exclusiones dinámicas**: Tipo C y semáforos de riesgo
- **Eventos de sujeto**: Creación y modificación de contribuyentes

---

## Casos de Prueba Ejecutados

### 1. Evento de Sujeto - CUIT 20-03034569-0

#### Evento de Entrada
```json
{
  "EV_ID": "20251009111546100000004274305555",
  "SUJ_IDENTIFICADOR": "20-03034569-0",
  "SUJ_CAT_SUJ_ID": "016",
  "SUJ_DENOMINACION": "TEST eventos descuento",
  "SUJ_ID_EXTERNO": "0303456",
  "SUJ_TIPO": "F",
  "SUJ_CANAL_ORIGEN": "OTAX"
}
```

#### Resultado
✅ **EXITOSO** - Persistencia correcta en Cassandra con marca por defecto en `true`

#### Validación en Base de Datos

```json
{
  "results" : [ {
    "suj_identificador" : "20-03034569-0",
    "suj_canal_origen" : "OTAX",
    "suj_cat_suj_id" : 16,
    "suj_denominacion" : "TEST eventos descuento",
    "suj_dfe" : null,
    "suj_direccion" : null,
    "suj_email" : null,
    "suj_id_externo" : "0303456",
    "suj_otros_atributos" : null,
    "suj_riesgo_fiscal" : null,
    "suj_saldo" : 0.0,
    "suj_situacion_fiscal" : null,
    "suj_telefono" : null,
    "suj_tiene30sujeto" : "true",
    "suj_tipo" : "F",
    "suj_tipo_exclusion" : null
  } ]
}
```

### 2. Evento de Objeto Tipo E - ID: 209517256

#### Evento de Entrada
```json
{
  "EV_ID": "20251009104950100000004274305555",
  "SOJ_SUJ_IDENTIFICADOR": "20-03034569-0",
  "SOJ_TIPO_OBJETO": "E",
  "SOJ_IDENTIFICADOR": "209517256",
  "SOJ_CAT_SOJ_ID": "TRI",
  "SOJ_DESCRIPCION": "Contribuyente Local",
  "SOJ_FECHA_FIN": "2025-08-31 00:00:00.0",
  "SOJ_FECHA_INICIO": "2007-05-01 00:00:00.0",
  "SOJ_ADHERIDO_DEBITO": "N",
  "SOJ_CANAL_ORIGEN": "OTAX",
  "SOJ_ID_EXTERNO": "655317"
}
```

#### Resultado
✅ **EXITOSO** - Proyección correcta en Cassandra con marcas por defecto en `true`

✅ **ESPERADO** - No se generan eventos en el tópico `dgr-cop-objeto-beneficios-v1` (correcto para objetos tipo E)

#### Validación Actor Vínculo

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034569-0",
    "soj_identificador" : "209517256",
    "soj_tipo_objeto" : "E",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "true",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "Contribuyente Local",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2025-08-31",
    "soj_fecha_inicio" : "2007-05-01",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "655317",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":null,\"PORCENTAJE_OTROS_ATRIBUTOS\":null,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009111842100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "true",
    "soj_tiene30objetovinculo" : "true",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```
Actor vínculo OK

```json
{
  "results" : [ {
    "soj_identificador" : "209517256",
    "soj_exclusion_objeto_vinculo" : null,
    "soj_map_transf" : "Map()",
    "soj_map_vinculo" : "Map(Vinculo(20-03034569-0,209517256,E) -> VinculoCotitular(true,Some(false),None,None))",
    "soj_tiene30objetovinculo" : "true",
    "soj_tipo_objeto" : "E"
  } ]
}
```

### 3. Evento de Obligación a Vencer

#### Evento de Entrada
```json
{
  "EV_ID": "20251009112812100000004274305555",
  "BOB_SUJ_IDENTIFICADOR": "20-03034569-0",
  "BOB_SOJ_TIPO_OBJETO": "E",
  "BOB_SOJ_IDENTIFICADOR": "209517256",
  "BOB_OBN_ID": "20250000000117412397",
  "BOB_SALDO": "15710",
  "BOB_CUOTA": "10",
  "BOB_ESTADO": "ADMINISTRATIVA",
  "BOB_PERIODO": "2025",
  "BOB_VENCIMIENTO": "2025-10-20 00:00:00.0",
  "BOB_CAPITAL": "15710",
  "BOB_CONCEPTO": "840",
  "BOB_IMPUESTO": "2"
}
```

#### Resultado
✅ **EXITOSO** - Persistencia correcta de la obligación y campo `bob_resultdmn`

```json
{
  "results" : [ {
    "bob_soj_identificador" : "209517256",
    "bob_soj_tipo_objeto" : "E",
    "bob_suj_identificador" : "20-03034569-0",
    "bob_obn_id" : "20250000000117412397",
    "bob_adherido_debito" : "N",
    "bob_canal_origen" : "OTAX",
    "bob_capital" : "15710",
    "bob_concepto" : "840",
    "bob_cuota" : "10",
    "bob_estado" : "ADMINISTRATIVA",
    "bob_exenta" : null,
    "bob_fechasancion" : null,
    "bob_fiscalizada" : "N",
    "bob_impuesto" : "2",
    "bob_indice_int_punit" : null,
    "bob_indice_int_resar" : null,
    "bob_interes_punit" : null,
    "bob_interes_resar" : null,
    "bob_jui_id" : null,
    "bob_oga_id" : "225840",
    "bob_otros_atributos" : {
      "BOB_DETALLES" : "[{\"BOB_MUNICIPIO\":null,\"RULE_NUMBER\":\"4\",\"tiene30Obligaciones\":false,\"BAND_BATCH\":false,\"EV_ID\":20251009112812100000004274305555,\"SOJ_ID_EXTERNO\":\"655317\",\"EVO_OBN_PEO_ID_MATERIAL\":\"PC\",\"JUICIO_MULTIOBJETO\":\"N\",\"BOB_INTERES_FINANCIACION\":null,\"EVO_OBN_PEO_ID_FORMAL\":\"NC\",\"PLAN_MULTIOBJETO\":\"N\",\"FLAG_OCULTA_WEB\":null,\"dmnNumero\":null,\"dmnDescripcion\":null,\"SOJ_FECHA_LABRADO\":null,\"SOJ_FECHA_SENTENCIA\":null,\"SOJ_FECHA_RESOLUCION\":null,\"SOJ_DESCUENTO_VIGENTE\":null}]"
    },
    "bob_periodo" : "2025",
    "bob_pln_id" : null,
    "bob_porcentaje_exencion" : null,
    "bob_prorroga" : "2025-10-20",
    "bob_resultdmn" : "Some((1,No Deuda))",
    "bob_saldo" : "15710",
    "bob_soj_identificador_2" : null,
    "bob_sub_estado" : null,
    "bob_supresiones" : null,
    "bob_tiene30obligacion" : "false",
    "bob_tipo" : "tributaria",
    "bob_total" : null,
    "bob_tpbid" : null,
    "bob_vencimiento" : "2025-10-20",
    "bob_vencimiento_2" : "2025-11-20"
  } ]
}
```

#### ❌ PROBLEMA CRÍTICO IDENTIFICADO
La marca del objeto cambia erróneamente a `false`. 

**Análisis**: Es una deuda a vencer, por lo tanto **NO debería penalizar** las marcas `soj_tiene30objeto` y `soj_aplicardescuento`.

**Estado actual**: La condonación del objeto se mantiene correcta (`soj_resultdmn`), pero las marcas de descuento se modifican incorrectamente.

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034569-0",
    "soj_identificador" : "209517256",
    "soj_tipo_objeto" : "E",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "false",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "Contribuyente Local",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2025-08-31",
    "soj_fecha_inicio" : "2007-05-01",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "655317",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":null,\"PORCENTAJE_OTROS_ATRIBUTOS\":null,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009111842100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "false",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```
Al cambiar la marca del objeto cambia la marca del sujeto -> Ese comportamiento está OK

```json
{
  "results" : [ {
    "suj_identificador" : "20-03034569-0",
    "suj_canal_origen" : "OTAX",
    "suj_cat_suj_id" : 16,
    "suj_denominacion" : "TEST eventos descuento",
    "suj_dfe" : null,
    "suj_direccion" : null,
    "suj_email" : null,
    "suj_id_externo" : "0303456",
    "suj_otros_atributos" : null,
    "suj_riesgo_fiscal" : null,
    "suj_saldo" : 0.0,
    "suj_situacion_fiscal" : null,
    "suj_telefono" : null,
    "suj_tiene30sujeto" : "false",
    "suj_tipo" : "F",
    "suj_tipo_exclusion" : null
  } ]
}
``` 
 No se generan eventos en el tópico de notificación porque es un objeto de tipo E

 Evento de pago de la obligación eliminá la obligación, vuelve las marcas a true -> comportamiento correcto


 Obligación vencida con solo saldo

 ```json
 {
	"EV_ID": "20251009114027100000004274305555",
	"BOB_SUJ_IDENTIFICADOR": "20-03034569-0",
	"BOB_SOJ_TIPO_OBJETO": "E",
	"BOB_SOJ_IDENTIFICADOR": "209517256",
	"BOB_OBN_ID": "20250000000117412397",
	"BOB_SALDO": "15710",
	"BOB_CUOTA": "10",
	"BOB_ESTADO": "ADMINISTRATIVA",
	"BOB_CANAL_ORIGEN": "OTAX",
	"BOB_FISCALIZADA": "N",
	"BOB_JUI_ID": "999",
	"BOB_PERIODO": "2025",
	"BOB_PLN_ID": "null",
	"BOB_PRORROGA": "2025-08-20 00:00:00.0",
	"BOB_TIPO": "tributaria",
	"BOB_VENCIMIENTO": "2025-08-20 00:00:00.0",
	"BOB_CAPITAL": "0",
	"BOB_CONCEPTO": "840",
	"BOB_IMPUESTO": "2",
	"BOB_ADHERIDO_DEBITO": "N",
	"BOB_OGA_ID": "225840",
	"BOB_VENCIMIENTO_2": "2025-09-20 00:00:00.0",
	"SOJ_ID_EXTERNO": "655317",
	"BOB_OTROS_ATRIBUTOS": {
		"BOB_DETALLES": [
			{
				"BOB_MUNICIPIO": "null",
				"EVO_OBN_PEO_ID_MATERIAL": "PC",
				"BOB_INTERES_FINANCIACION": "null",
				"JUICIO_MULTIOBJETO": "N",
				"RULE_NUMBER": "4",
				"EVO_OBN_PEO_ID_FORMAL": "NC",
				"PLAN_MULTIOBJETO": "N"
			}
		]
	}
}
```

Obligación persistida OK y comportamiento de DMN OK

```json
{
  "results" : [ {
    "bob_soj_identificador" : "209517256",
    "bob_soj_tipo_objeto" : "E",
    "bob_suj_identificador" : "20-03034569-0",
    "bob_obn_id" : "20250000000117412397",
    "bob_adherido_debito" : "N",
    "bob_canal_origen" : "OTAX",
    "bob_capital" : "0",
    "bob_concepto" : "840",
    "bob_cuota" : "10",
    "bob_estado" : "ADMINISTRATIVA",
    "bob_exenta" : null,
    "bob_fechasancion" : null,
    "bob_fiscalizada" : "N",
    "bob_impuesto" : "2",
    "bob_indice_int_punit" : null,
    "bob_indice_int_resar" : null,
    "bob_interes_punit" : null,
    "bob_interes_resar" : null,
    "bob_jui_id" : null,
    "bob_oga_id" : "225840",
    "bob_otros_atributos" : {
      "BOB_DETALLES" : "[{\"BOB_MUNICIPIO\":null,\"RULE_NUMBER\":\"4\",\"tiene30Obligaciones\":false,\"BAND_BATCH\":false,\"EV_ID\":20251009114027100000004274305555,\"SOJ_ID_EXTERNO\":\"655317\",\"EVO_OBN_PEO_ID_MATERIAL\":\"PC\",\"JUICIO_MULTIOBJETO\":\"N\",\"BOB_INTERES_FINANCIACION\":null,\"EVO_OBN_PEO_ID_FORMAL\":\"NC\",\"PLAN_MULTIOBJETO\":\"N\",\"FLAG_OCULTA_WEB\":null,\"dmnNumero\":null,\"dmnDescripcion\":null,\"SOJ_FECHA_LABRADO\":null,\"SOJ_FECHA_SENTENCIA\":null,\"SOJ_FECHA_RESOLUCION\":null,\"SOJ_DESCUENTO_VIGENTE\":null}]"
    },
    "bob_periodo" : "2025",
    "bob_pln_id" : null,
    "bob_porcentaje_exencion" : null,
    "bob_prorroga" : "2025-08-20",
    "bob_resultdmn" : "Some((-6,Obligaciones vencidas del Impuesto sobre los Ingresos Brutos,  Régimen Simplificado, en instancia de gestión administrativa o prejudicial (MUC)))",
    "bob_saldo" : "15710",
    "bob_soj_identificador_2" : null,
    "bob_sub_estado" : null,
    "bob_supresiones" : null,
    "bob_tiene30obligacion" : "false",
    "bob_tipo" : "tributaria",
    "bob_total" : null,
    "bob_tpbid" : null,
    "bob_vencimiento" : "2025-08-20",
    "bob_vencimiento_2" : "2025-09-20"
  } ]
}
```

Modifica OK las marcas del objeto

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034569-0",
    "soj_identificador" : "209517256",
    "soj_tipo_objeto" : "E",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "false",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,true,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "Contribuyente Local",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2025-08-31",
    "soj_fecha_inicio" : "2007-05-01",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "655317",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":null,\"PORCENTAJE_OTROS_ATRIBUTOS\":null,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009111842100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "false",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```

Cambio OK de la marca del sujeto

```json
{
  "results" : [ {
    "suj_identificador" : "20-03034569-0",
    "suj_canal_origen" : "OTAX",
    "suj_cat_suj_id" : 16,
    "suj_denominacion" : "TEST eventos descuento",
    "suj_dfe" : null,
    "suj_direccion" : null,
    "suj_email" : null,
    "suj_id_externo" : "0303456",
    "suj_otros_atributos" : null,
    "suj_riesgo_fiscal" : null,
    "suj_saldo" : 0.0,
    "suj_situacion_fiscal" : null,
    "suj_telefono" : null,
    "suj_tiene30sujeto" : "false",
    "suj_tipo" : "F",
    "suj_tipo_exclusion" : null
  } ]
}
```

Evento de Objeto A

```json
{
	"EV_ID": "20251009114624100000004274305555",
	"SOJ_SUJ_IDENTIFICADOR": "20-03034569-0",
	"SOJ_TIPO_OBJETO": "A",
	"SOJ_IDENTIFICADOR": "AUTO0910",
	"SOJ_CAT_SOJ_ID": "TRI",
	"SOJ_DESCRIPCION": "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
	"SOJ_ESTADO": "null",
	"SOJ_FECHA_ADQ_SUBASTA": "1000-01-01 00:00:00.0",
	"SOJ_FECHA_VTA_SUBASTA": "1000-01-01 00:00:00.0",
	"SOJ_FECHA_FIN": "2024-08-09 00:00:00.0",
	"SOJ_FECHA_INICIO": "2012-09-10 00:00:00.0",
	"SOJ_SUBTIPO": "null",
	"SOJ_ADHERIDO_DEBITO": "N",
	"SOJ_CANAL_ORIGEN": "OTAX",
	"SOJ_TITULARIDAD": "null",
	"SOJ_ID_EXTERNO": "7951142",
	"SOJ_OTROS_ATRIBUTOS": {
		"SOJ_DETALLES": [
			{
				"RESPONSABLE_OTROS_ATRIBUTOS": "S",
				"PORCENTAJE_OTROS_ATRIBUTOS": "100",
				"OTROS_ATRIBUTOS_ADHERIDO_DEBITO": "N",
				"CUENTA_SOJ_OTROS_ATRIBUTOS": "null",
				"PERIODO_SOJ_OTROS_ATRIBUTOS": "null",
				"IMPORTE_SOJ_OTROS_ATRIBUTOS": "null",
				"SOJ_SEMAFORO_COLOR": "null",
				"SOJ_SEMAFORO_MARCA": "null"
			}
		]
	}
}
```

Ingresa al tópico el evento del objeto marcado en false correctamente ya que al condonar por sujeto el E le quita el 30 por más de no tener deuda en este Objeto A, pero sin `dmnDescripcion`
```json
{
  "deliveryId" : 20251009114624100000004274305555,
  "sujetoId" : "20-03034569-0",
  "objetoId" : "AUTO0910",
  "tipoObjeto" : "A",
  "idExterno" : "7951142",
  "fecha" : "2025-10-09 14:46:26.3",
  "beneficios" : [
    {
      "codigo" : "DTO30",
      "aplicarDescuento" : false,
      "dmnNumero" : null,
      "dmnDescripcion" : ""
    }
  ]
}
```

Obligación a vencer para el auto correctamente persistida y evaluada por el DMN

```json
{
  "results" : [ {
    "bob_soj_identificador" : "AUTO0910",
    "bob_soj_tipo_objeto" : "A",
    "bob_suj_identificador" : "20-03034569-0",
    "bob_obn_id" : "20250000000005413850",
    "bob_adherido_debito" : "N",
    "bob_canal_origen" : "OTAX",
    "bob_capital" : "31367.69",
    "bob_concepto" : "601",
    "bob_cuota" : "8",
    "bob_estado" : "ADMINISTRATIVA",
    "bob_exenta" : null,
    "bob_fechasancion" : null,
    "bob_fiscalizada" : "N",
    "bob_impuesto" : "600",
    "bob_indice_int_punit" : null,
    "bob_indice_int_resar" : null,
    "bob_interes_punit" : null,
    "bob_interes_resar" : null,
    "bob_jui_id" : null,
    "bob_oga_id" : "62501",
    "bob_otros_atributos" : {
      "BOB_DETALLES" : "[{\"BOB_MUNICIPIO\":null,\"RULE_NUMBER\":\"14\",\"tiene30Obligaciones\":false,\"BAND_BATCH\":false,\"EV_ID\":20251009115245100000004274305555,\"SOJ_ID_EXTERNO\":\"7951142\",\"EVO_OBN_PEO_ID_MATERIAL\":\"PC\",\"JUICIO_MULTIOBJETO\":\"N\",\"BOB_INTERES_FINANCIACION\":null,\"EVO_OBN_PEO_ID_FORMAL\":\"NC\",\"PLAN_MULTIOBJETO\":\"N\",\"FLAG_OCULTA_WEB\":null,\"dmnNumero\":null,\"dmnDescripcion\":null,\"SOJ_FECHA_LABRADO\":null,\"SOJ_FECHA_SENTENCIA\":null,\"SOJ_FECHA_RESOLUCION\":null,\"SOJ_DESCUENTO_VIGENTE\":null}]"
    },
    "bob_periodo" : "2025",
    "bob_pln_id" : null,
    "bob_porcentaje_exencion" : null,
    "bob_prorroga" : "2025-10-10",
    "bob_resultdmn" : "Some((1,No Deuda))",
    "bob_saldo" : "31367.69",
    "bob_soj_identificador_2" : null,
    "bob_sub_estado" : null,
    "bob_supresiones" : null,
    "bob_tiene30obligacion" : "false",
    "bob_tipo" : "tributaria",
    "bob_total" : null,
    "bob_tpbid" : null,
    "bob_vencimiento" : "2025-10-10",
    "bob_vencimiento_2" : null
  } ]
}
```
No se modifican las marcas ni a nivel sujeto ni a nivel objeto -> OK
No ingresan eventos al tópico `dgr-cop-objeto-beneficios-v1` por que no se modificó la marca -> OK

Se envía evento de cotitularidad para el AUTO0910 sujeto 20-03034568-0

El evento persiste OK

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034569-0",
    "soj_identificador" : "AUTO0910",
    "soj_tipo_objeto" : "A",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "false",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2024-08-09",
    "soj_fecha_inicio" : "2012-09-10",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "7951142",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"S\",\"PORCENTAJE_OTROS_ATRIBUTOS\":100,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009114624100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "false",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  }, {
    "soj_suj_identificador" : "20-03034568-0",
    "soj_identificador" : "AUTO0910",
    "soj_tipo_objeto" : "A",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "true",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2024-08-09",
    "soj_fecha_inicio" : "2012-09-10",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "7951142",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"N\",\"PORCENTAJE_OTROS_ATRIBUTOS\":30,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009115812100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "true",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```

Actor Vínculo

```json
{
  "results" : [ {
    "soj_identificador" : "AUTO0910",
    "soj_exclusion_objeto_vinculo" : null,
    "soj_map_transf" : "Map()",
    "soj_map_vinculo" : "Map(Vinculo(20-03034569-0,AUTO0910,A) -> VinculoCotitular(false,Some(true),None,None), Vinculo(20-03034568-0,AUTO0910,A) -> VinculoCotitular(true,Some(false),None,None))",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_objeto" : "A"
  } ]
}
```

soj_tiene30objeto y soj_aplicardescuento aparecen en true para el segundo vínculo, lo que es correcto, pero no para el primero
No cambia el tipo de condonación ya que ahora al ser cotitularidad debe condonar por objeto, no por sujeto 
Entiendo que esto es lo que hace que se lleve el error a la marca `soj_tiene30objetovinculo`que ahora debería ser true

Sujeto 2 20-03034769-2
Sujeto y objeto OK

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034769-2",
    "soj_identificador" : "AUTO09102",
    "soj_tipo_objeto" : "A",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "true",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2024-08-09",
    "soj_fecha_inicio" : "2012-09-10",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "7951142",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"S\",\"PORCENTAJE_OTROS_ATRIBUTOS\":100,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009122054100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "true",
    "soj_tiene30objetovinculo" : "true",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```

Obligación vencida para el auto

```json
{
  "results" : [ {
    "bob_soj_identificador" : "AUTO09102",
    "bob_soj_tipo_objeto" : "A",
    "bob_suj_identificador" : "20-03034769-2",
    "bob_obn_id" : "20250000000005413850",
    "bob_adherido_debito" : "N",
    "bob_canal_origen" : "OTAX",
    "bob_capital" : "31367.69",
    "bob_concepto" : "601",
    "bob_cuota" : "7",
    "bob_estado" : "ADMINISTRATIVA",
    "bob_exenta" : null,
    "bob_fechasancion" : null,
    "bob_fiscalizada" : "N",
    "bob_impuesto" : "600",
    "bob_indice_int_punit" : null,
    "bob_indice_int_resar" : null,
    "bob_interes_punit" : null,
    "bob_interes_resar" : null,
    "bob_jui_id" : null,
    "bob_oga_id" : "62501",
    "bob_otros_atributos" : {
      "BOB_DETALLES" : "[{\"BOB_MUNICIPIO\":null,\"RULE_NUMBER\":\"14\",\"tiene30Obligaciones\":false,\"BAND_BATCH\":false,\"EV_ID\":20251009123612100000004274305555,\"SOJ_ID_EXTERNO\":\"7951142\",\"EVO_OBN_PEO_ID_MATERIAL\":\"PC\",\"JUICIO_MULTIOBJETO\":\"N\",\"BOB_INTERES_FINANCIACION\":null,\"EVO_OBN_PEO_ID_FORMAL\":\"NC\",\"PLAN_MULTIOBJETO\":\"N\",\"FLAG_OCULTA_WEB\":null,\"dmnNumero\":null,\"dmnDescripcion\":null,\"SOJ_FECHA_LABRADO\":null,\"SOJ_FECHA_SENTENCIA\":null,\"SOJ_FECHA_RESOLUCION\":null,\"SOJ_DESCUENTO_VIGENTE\":null}]"
    },
    "bob_periodo" : "2025",
    "bob_pln_id" : null,
    "bob_porcentaje_exencion" : null,
    "bob_prorroga" : "2025-08-10",
    "bob_resultdmn" : "Some((-8,Obligaciones vencidas del Impuesto a la Propiedad Automotor en instancia de gestión administrativa o prejudicial))",
    "bob_saldo" : "31367.69",
    "bob_soj_identificador_2" : null,
    "bob_sub_estado" : null,
    "bob_supresiones" : null,
    "bob_tiene30obligacion" : "false",
    "bob_tipo" : "tributaria",
    "bob_total" : null,
    "bob_tpbid" : null,
    "bob_vencimiento" : "2025-08-10",
    "bob_vencimiento_2" : null
  } ]
}
```
Cambiaron correctamente las marcas del 30 a nivel objeto y sujeto.
Ingresa correctamente el evento al tópico pero sin la razón

```json
{
  "deliveryId" : 20251009122054100000004274305555,
  "sujetoId" : "20-03034769-2",
  "objetoId" : "AUTO09102",
  "tipoObjeto" : "A",
  "idExterno" : "7951142",
  "fecha" : "2025-10-09 15:36:14.9",
  "beneficios" : [
    {
      "codigo" : "DTO30",
      "aplicarDescuento" : false,
      "dmnNumero" : null,
      "dmnDescripcion" : null
    }
  ]
}
```

Evento de INMUEBLE con semáforo rojo

```json
{
	"EV_ID": "20251009124055100000004274305555",
	"SOJ_SUJ_IDENTIFICADOR": "20-03034769-2",
	"SOJ_TIPO_OBJETO": "I",
	"SOJ_IDENTIFICADOR": "03034560303",
	"SOJ_DESCRIPCION": "LOS REMANSO (1448) - NONO - SAN ALBERTO     ",
	"SOJ_ESTADO": "null",
	"SOJ_FECHA_ADQ_SUBASTA": "1000-01-01 00:00:00.0",
	"SOJ_FECHA_VTA_SUBASTA": "1000-01-01 00:00:00.0",
	"SOJ_FECHA_FIN": "1000-01-01 00:00:00.0",
	"SOJ_FECHA_INICIO": "1901-01-01 00:00:00.0",
	"SOJ_SUBTIPO": "urbano",
	"SOJ_ADHERIDO_DEBITO": "N",
	"SOJ_CANAL_ORIGEN": "OTAX",
	"SOJ_CAT_SOJ_ID": "TRI",
	"SOJ_TITULARIDAD": "null",
	"SOJ_ID_EXTERNO": "0303456",
	"SOJ_OTROS_ATRIBUTOS": {
		"SOJ_DETALLES": [
			{
				"RESPONSABLE_OTROS_ATRIBUTOS": "S",
				"PORCENTAJE_OTROS_ATRIBUTOS": "100",
				"OTROS_ATRIBUTOS_ADHERIDO_DEBITO": "N",
				"SOJ_SEMAFORO_COLOR": "R",
				"SOJ_SEMAFORO_MARCA": "M"
			}
		]
	}
}
``` 

Evento persistido correctamente y cambia OK la condonación ya que tiene semáforo rojo

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034769-2",
    "soj_identificador" : "03034560303",
    "soj_tipo_objeto" : "I",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "true",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "LOS REMANSO (1448) - NONO - SAN ALBERTO     ",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : null,
    "soj_fecha_inicio" : "1901-01-01",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "0303456",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"S\",\"PORCENTAJE_OTROS_ATRIBUTOS\":100,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":\"R\",\"SOJ_SEMAFORO_MARCA\":\"M\",\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009124055100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 1,
    "soj_saldo" : 0.0,
    "soj_subtipo" : "urbano",
    "soj_tiene30objeto" : "true",
    "soj_tiene30objetovinculo" : "true",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  }, {
    "soj_suj_identificador" : "20-03034769-2",
    "soj_identificador" : "AUTO09102",
    "soj_tipo_objeto" : "A",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "false",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2024-08-09",
    "soj_fecha_inicio" : "2012-09-10",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "7951142",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"S\",\"PORCENTAJE_OTROS_ATRIBUTOS\":100,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009122054100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "false",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```

Ingresa el evento del nuevo objeto

```json
{
  "deliveryId" : 20251009124055100000004274305555,
  "sujetoId" : "20-03034769-2",
  "objetoId" : "03034560303",
  "tipoObjeto" : "I",
  "idExterno" : "0303456",
  "fecha" : "2025-10-09 15:40:58.5",
  "beneficios" : [
    {
      "codigo" : "DTO30",
      "aplicarDescuento" : true,
      "dmnNumero" : null,
      "dmnDescripcion" : ""
    }
  ]
}
```
Al cambiar el color del semáforo se modificó OK la marca y tipo de condonación


Evento de exclusion por objeto de tipo C

```json
{
	"EV_ID": "20251009124728100000004274305555",
	"SOJ_SUJ_IDENTIFICADOR": "20-03034769-2",
	"SOJ_TIPO_OBJETO": "I",
	"SOJ_IDENTIFICADOR": "03034560303",
	"SOJ_TIPO_EXCLUSION": "C"
}
```
Cambian correctamente las marcas del 30 y tipo de condonación.
El evento se dispara correctamente y con la descripción

```json
{
  "deliveryId" : 20251009124728100000004274305555,
  "sujetoId" : "20-03034769-2",
  "objetoId" : "03034560303",
  "tipoObjeto" : "I",
  "idExterno" : "0303456",
  "fecha" : "2025-10-09 15:47:28.3",
  "beneficios" : [
    {
      "codigo" : "DTO30",
      "aplicarDescuento" : true,
      "dmnNumero" : null,
      "dmnDescripcion" : "Objeto Condicional"
    }
  ]
}
```

Alo vencer la exclusión las marcas cambian OK y se dispara el evento pero sin descripción

```json
{
  "deliveryId" : 20251009124947100000004274305555,
  "sujetoId" : "20-03034769-2",
  "objetoId" : "03034560303",
  "tipoObjeto" : "I",
  "idExterno" : "0303456",
  "fecha" : "2025-10-09 15:49:50.1",
  "beneficios" : [
    {
      "codigo" : "DTO30",
      "aplicarDescuento" : false,
      "dmnNumero" : null,
      "dmnDescripcion" : ""
    }
  ]
}
```

### 8. Transferencia de AUTO con Deuda Existente

#### ❌ PROBLEMA CRÍTICO - Evaluación Incorrecta en Transferencias

**Descripción**: Se realizó la transferencia del AUTO09102 que ya tenía deuda vencida.

**Evento generado** (INCORRECTO):
```json
{
  "deliveryId": 20251009125504100000004274305555,
  "sujetoId": "20-03034779-2",
  "objetoId": "AUTO09102",
  "tipoObjeto": "A",
  "idExterno": "7951142",
  "fecha": "2025-10-09 15:55:09.2",
  "beneficios": [
    {
      "codigo": "DTO30",
      "aplicarDescuento": true,  // ❌ INCORRECTO - Debería ser false
      "dmnNumero": null,
      "dmnDescripcion": ""
    }
  ]
}
```

**Problema**: La marca del objeto no es correcta. El sistema **no está evaluando la deuda preexistente** al momento de la transferencia.

```json
{
  "results" : [ {
    "soj_suj_identificador" : "20-03034769-2",
    "soj_identificador" : "AUTO09102",
    "soj_tipo_objeto" : "A",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "false",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2024-08-09",
    "soj_fecha_inicio" : "2012-09-10",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "7951142",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"S\",\"PORCENTAJE_OTROS_ATRIBUTOS\":100,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009122054100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "false",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  }, {
    "soj_suj_identificador" : "20-03034779-2",
    "soj_identificador" : "AUTO09102",
    "soj_tipo_objeto" : "A",
    "soj_adherido_debito" : "N",
    "soj_aplicardescuento" : "true",
    "soj_base_imponible" : null,
    "soj_canal_origen" : "OTAX",
    "soj_cant_cuotas_pagadas" : "[false,false,false,false,false,false,false,false,false,false,false,false,false]",
    "soj_cat_soj_id" : "TRI",
    "soj_cotitular_suj_identificador" : null,
    "soj_descripcion" : "RENAULT TRANSPORTE DE PASAJEROS MASTER PH3 DCI 120 PKLUX",
    "soj_documento" : null,
    "soj_estado" : null,
    "soj_etiquetas" : null,
    "soj_fecha_adq_subasta" : null,
    "soj_fecha_fin" : "2024-08-09",
    "soj_fecha_inicio" : "2012-09-10",
    "soj_fecha_vta_subasta" : null,
    "soj_id_externo" : "7951142",
    "soj_identificador_2" : null,
    "soj_otros_atributos" : {
      "SOJ_DETALLES" : "[{\"RESPONSABLE_OTROS_ATRIBUTOS\":\"S\",\"PORCENTAJE_OTROS_ATRIBUTOS\":100,\"CUENTA_SOJ_OTROS_ATRIBUTOS\":null,\"OTROS_ATRIBUTOS_ADHERIDO_DEBITO\":\"N\",\"PERIODO_SOJ_OTROS_ATRIBUTOS\":null,\"IMPORTE_SOJ_OTROS_ATRIBUTOS\":null,\"SOJ_SEMAFORO_COLOR\":null,\"SOJ_SEMAFORO_MARCA\":null,\"SOJ_ADQUIRIDO_SUBASTA\":null,\"FECHA_SUBASTA\":null,\"SOJ_OWNER\":null,\"EV_ID\":20251009125504100000004274305555}]"
    },
    "soj_porcentaje_cotitular" : null,
    "soj_resultdmn" : 2,
    "soj_saldo" : 0.0,
    "soj_subtipo" : null,
    "soj_tiene30objeto" : "true",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_exclusion" : null,
    "soj_titularidad" : null
  } ]
}
```

Actor vínculo

```json
{
  "results" : [ {
    "soj_identificador" : "AUTO09102",
    "soj_exclusion_objeto_vinculo" : null,
    "soj_map_transf" : "Map(Vinculo(20-03034769-2,AUTO09102,A) -> VinculoCotitular(false,Some(true),None,Some(TRANSF)))",
    "soj_map_vinculo" : "Map(Vinculo(20-03034779-2,AUTO09102,A) -> VinculoCotitular(true,Some(true),None,None))",
    "soj_tiene30objetovinculo" : "false",
    "soj_tipo_objeto" : "A"
  } ]
}
```

---

## Problemas Identificados

### 🔴 CRÍTICO - Prioridad Máxima

#### 1. Penalización incorrecta ante obligaciones a vencer
**Descripción**: El sistema penaliza incorrectamente las marcas del 30% cuando se registran obligaciones a vencer.

**Problema**: Es una deuda a vencer, por lo tanto **NO debería penalizar** las marcas `soj_tiene30objeto` y `soj_aplicardescuento`.

**Estado actual**: 
- ✅ A nivel obligación se evalúa correctamente 
- ❌ A nivel objeto las marcas cambian incorrectamente a `false`

**Caso específico**: Obligación ID `20250000000117412397` con vencimiento `2025-10-20` (futura) genera penalización indebida.

#### 2. Campo `clasificar30Objeto` ausente o mal configurado
**Descripción**: El campo `clasificar30Objeto` no aparece correctamente en la tabla `buc_sujeto_objeto`. Si fue reemplazado por `soj_resultdmn`, la lógica no funciona correctamente.

**Comportamiento esperado**:
- Valor `1`: Condonación por objeto (análisis de deuda por objeto individual)
- Valor `2`: Condonación por sujeto (análisis de deuda por todo el sujeto)

**Problema observado**: En las pruebas el valor se mantuvo siempre en 2, incluso en cotitularidades. Solo cambió correctamente ante semáforo rojo y exclusión tipo C.

#### 3. Evaluación incorrecta en transferencias
**Descripción**: Al realizar transferencias de objetos con deuda, la marca del 30% no evalúa correctamente la deuda existente, otorgando incorrectamente el descuento.

**Problema específico**: No se respeta la regla de **responsabilidad solidaria** durante las transferencias.

**Caso observado**: 
- Objeto: AUTO09102 
- Estado: Deuda vencida existente
- Resultado: Se transfiere con marca `aplicarDescuento: true` 
- **Esperado**: Debería ser `aplicarDescuento: false`

**Impacto**: Pérdida de recaudación por descuentos aplicados incorrectamente.

### 🟡 MEDIO - Prioridad Media

#### 4. Falta información en eventos del tópico
**Descripción**: Los eventos enviados al tópico `dgr-cop-objeto-beneficios-v1` no incluyen la razón (`dmnDescripcion`) de por qué se aplica o no el descuento.

**Impacto**: Dificulta la trazabilidad y debugging del sistema.

---

## Funcionalidades Validadas ✅

| Funcionalidad | Estado | Observaciones |
|---------------|--------|---------------|
| Evaluación DMN por saldo | ✅ Correcto | Funciona según especificación |
| Envío de eventos al tópico | ✅ Correcto | Solo se envían cuando cambia la marca |
| Manejo de exclusiones tipo C | ✅ Correcto | Cambios correctos en condonación y descripción |
| Comportamiento con semáforo rojo | ✅ Correcto | Aplicabilidad del 30% correcta |
| Persistencia en Cassandra | ✅ Correcto | Todos los eventos persisten correctamente |
| Eventos de pago | ✅ Correcto | Elimina obligaciones y restaura marcas |
| Vencimiento de exclusiones | ✅ Correcto | Restaura estado correcto del objeto |
| Cotitularidades básicas | ✅ Correcto | Creación y gestión de vínculos |

---

## Conclusiones y Recomendaciones

### Acciones Inmediatas Requeridas

1. **Corregir lógica de penalización** para obligaciones a vencer (no deben penalizar)
2. **Revisar y corregir** la lógica del campo `soj_resultdmn` para indicar el tipo de condonación
3. **Implementar validación** de deuda existente en transferencias de objetos (responsabilidad solidaria)
4. **Agregar información de razón** en eventos del tópico de beneficios

### Pruebas Adicionales Recomendadas

Antes del despliegue se recomienda ejecutar los siguientes casos adicionales:

1. **Obligaciones a vencer con diferentes plazos** (30, 60, 90 días)
2. **Transferencias múltiples** del mismo objeto entre diferentes sujetos
3. **Combinaciones de exclusiones** (tipo C + semáforo rojo)
4. **Responsabilidad solidaria** en diferentes escenarios de cotitularidad
5. **Volumen de transacciones** para validar performance

### Impacto en Producción

Los problemas identificados pueden resultar en:

**🔴 Impacto Financiero:**
- Pérdida de recaudación por descuentos aplicados incorrectamente en transferencias
- Penalización indebida a contribuyentes con obligaciones a vencer
- Inconsistencias en el cálculo del 30% de descuento

**🟡 Impacto Operativo:**
- Falta de trazabilidad en decisiones del sistema (sin `dmnDescripcion`)
- Dificultades en debugging y resolución de reclamos
- Inconsistencias en el manejo de cotitularidades y responsabilidad solidaria

**📊 Estimación de Riesgo:**
- **Alto**: Problemas 1 y 3 (impacto directo en recaudación)
- **Medio**: Problema 2 (afecta lógica de negocio)
- **Bajo**: Problema 4 (impacto en trazabilidad)

### Recomendación Final

⚠️ **NO PROCEDER CON EL DESPLIEGUE** hasta resolver los problemas críticos identificados.

**Prioridad de corrección:**
1. **Inmediato**: Problema 1 (penalización obligaciones a vencer)
2. **Inmediato**: Problema 3 (evaluación en transferencias)
3. **Siguiente sprint**: Problema 2 (campo clasificar30Objeto)
4. **Backlog**: Problema 4 (información en eventos)

**Criterios de aceptación para despliegue:**
- ✅ Obligaciones a vencer no penalizan marcas del 30%
- ✅ Transferencias respetan responsabilidad solidaria
- ✅ Regresión exitosa de todos los casos de prueba
- ✅ Validación en ambiente de pre-producción

---

## Información Técnica

### Ambiente de Pruebas
- **Ambiente**: DESA (Desarrollo)
- **Fecha de ejecución**: 09/10/2025
- **Base de datos**: Cassandra
- **Tópico de eventos**: `dgr-cop-objeto-beneficios-v1`

### Casos de Prueba Detallados
- Eventos de sujeto y objetos tipo E, A, I
- Obligaciones a vencer y vencidas
- Pagos de obligaciones
- Transferencias y cotitularidades
- Exclusiones tipo C y semáforos rojos
- Validación de persistencia en Cassandra
- Verificación de eventos en tópicos

---