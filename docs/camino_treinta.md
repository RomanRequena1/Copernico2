# 🎯 RESUMEN COMPLETO DEL FLUJO - Sistema de Descuento 30%

---

## 📊 ARQUITECTURA GLOBAL

```
KAFKA → OBLIGACIÓN → OBJETO → OBJETO_VINCULO → OBJETO → SUJETO → OBJETO (final)
         ↓ DMN        ↓ State    ↓ Cotitulares   ↓ State  ↓ State  ↓ DMN Final
      tiene30Obn   tiene30Obj   tiene30ObjVinc           tiene30Suj  aplicarDescuento
```

---

## 🔄 FLUJO DETALLADO POR ENTIDAD

### 1️⃣ **OBLIGACIÓN** (Kafka Consumer)

#### **Consumer: `ObligacionTributariaTransaction`**
```scala
Kafka Topic: "DGR-COP-OBLIGACIONES-TRI"
↓
JSON: ObligacionesTri
↓
Validaciones iniciales:
  - Campos obligatorios vacíos → Error
  - RULE_NUMBER = "-1" o "-2" → ObligacionRemove
  - Otros casos → Evalúa DMN
```

#### **DMN Evaluation: `isTreintaPorciento()`**
```scala
DMN: decision_30_descuento.dmn
↓
Resultado:
  - numero = 1  → tiene30Obligaciones = true  (NO deuda, SÍ descuento)
  - numero < 0  → tiene30Obligaciones = false (SÍ deuda, NO descuento)
```

#### **Handler: `ObligacionUpdateFromDtoHandler`**
```scala
Crea evento: ObligacionUpdatedFromDto
↓
Actualiza state: ObligacionState
↓
Bifurcación:
  - tiene30Obligaciones = true  → informParent(command)
  - tiene30Obligaciones = false → informParentTreintaProciento(event)
```

**Ambos envían:**
```scala
ObjetoCommands.ObjetoUpdateFromObligacion         // tiene30 = true
ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento // tiene30 = false
```

---

### 2️⃣ **OBJETO** (Primera Actualización desde Obligación)

#### **Handlers:**
1. `ObjetoUpdateFromObligacionHandler` (tiene30 = true)
2. `ObjetoUpdateFromObligacionTreintaProcientoHandler` (tiene30 = false)

#### **Actualización de State:**
```scala
obnVencidas: Map[String, Boolean]
  - true  → Obligación NO deuda (tiene30Obn = true)
  - false → Obligación SÍ deuda (tiene30Obn = false)

tiene30Objeto = diffCurrentStateAndNewState(obnVencidas)
  ↓
  if (obnVencidas.values.forall(_ == true)) true else false
  // ⚠️ SOLO verifica obligaciones, NO usa tiene30ObjetoVinculo
```

#### **Envío a ObjetoVinculo:**
```scala
SendObjetoToObjetoVinculo(...) {
  match estado:
    TRANSF → CreateTransfVinculoObjetoFromObj
    BAJA   → RemoveObjetoVinculo
    _      → UpdateVinculoObjetoFromObj
}
```

**Si estado = TRANSF, bifurcación adicional:**
```scala
if (tiene30Objeto == false)
  actor.informParentTreintaPorciento(...) → SujetoUpdateFromObjetoTreintaPorciento
else
  actor.informParent(...)                 → SujetoUpdateFromObjeto
```

---

### 3️⃣ **OBJETO_VINCULO** (Gestión de Cotitulares)

#### **Handlers:**
1. `CreateVinculoObjetoFromObjTranfHandler`
2. `UpdateObjetoVinculoFromObjHandler`

#### **Estructura del State:**
```scala
mapVinculo: Map[Vinculo, VinculoCotitular]  // Vínculos normales
mapTransf:  Map[Vinculo, VinculoCotitular]  // Transferencias responsables

Vinculo(sujetoId, objetoId, tipoObj)
VinculoCotitular(tiene30Objeto, isResponsable, titularidad, estadoObj)
```

#### **Cálculo del tiene30ObjetoVinculo:**
```scala
calcular30desdeMapVinculo(_mapVinculo, _mapTransf) = {
  if (_mapTransf.isEmpty) {
    _mapVinculo.forall(_._2.tiene30Objeto)  // Solo mapVinculo
  } else {
    _mapVinculo.forall(_._2.tiene30Objeto) && 
    _mapTransf.forall(_._2.tiene30Objeto)   // Ambos maps
  }
}
```

#### **Notificación a TODOS los objetos vinculados:**
```scala
actor.state.mapVinculo.foreach { e =>
  actorSujetoGeneral.ask[Response.SuccessProcessing](
    UpdateState30ObjetoFromObjVinculo(
      e._1.sujetoId, 
      e._1.objetoId, 
      e._1.tipoObj,
      actor.state.tiene30ObjetoVinculo,  // ← Resultado consolidado
      command.exclusionObjeto
    )
  )
}
```

---

### 4️⃣ **OBJETO** (Segunda Actualización desde ObjetoVinculo)

#### **Handler: `UpdateState30ObjetoFromObjVinculoHandler`**
```scala
Crea evento: UpdatedState30ObjetoFromObjVinculo
↓
Actualiza state:
  - tiene30ObjetoVinculo = command.tiene30ObjetoVinculo
↓
Bifurcación hacia SUJETO:
  - tiene30Objeto = false → SendToSujeto(...)  → SujetoUpdateFromObjetoTreintaPorciento
  - tiene30Objeto = true  → SendToSujeto1(...) → SujetoUpdateFromObjeto
```

---

### 5️⃣ **SUJETO** (Consolidación de Objetos)

#### **Handlers:**
1. `SujetoUpdateFromObjetoHandler` (tiene30Obj = true)
2. `SujetoUpdateFromObjetoTreintaProcientoHandler` (tiene30Obj = false)

#### **Actualización de State:**
```scala
objVencidas: Map[String, (Boolean, String)]
  - (true, "clasificacionObjeto")  → Objeto con descuento
  - (false, "clasificacionObjeto") → Objeto sin descuento

tiene30Sujeto = diffCurrentStateAndNewState(...) {
  val map = newObjVencidas.filter(obj => !obj._2._2.equals("1")) // Excluye clase 1
  if (map.values.forall(_._1)) true else false
}
```

#### **Envío de vuelta a Objeto:**
```scala
SendToObjeto(actor.state, ...) {
  ObjetoCommands.ObjetoUpdateFromSujeto(
    tiene30Sujeto,
    exclusionSujeto,
    dmnDescripcionSujeto
  )
}
```

**⚠️ AMBOS handlers usan el MISMO comando, los caminos se UNEN aquí**

---

### 6️⃣ **OBJETO** (Actualización Final y DMN)

#### **Handler: `ObjetoUpdateFromSujetoHandler`**

**1. Validación inicial:**
```scala
if (estado == "TRANSF") return // No procesa objetos en transferencia
```

**2. Actualiza con info del Sujeto:**
```scala
event = ObjetoUpdatedFromSujeto(
  tiene30Sujeto,
  exclusionSujeto,
  dmnDescripcionSujeto
)
actor.state += event
```

**3. DMN FINAL: `DMNTreintaPorcientoFinal.calcularDmnFinal()`**
```scala
DmnFinal(
  suj_exclusionSujeto,        // Del sujeto
  soj_exclusionObjeto,        // Del objeto
  soj_clasificacionObjeto,    // "1" o "2"
  soj_deuda30Objeto,          // tiene30Objeto
  suj_deuda30Sujeto,          // tiene30Sujeto
  tiene30ObjetoVinculo        // De cotitulares
)
↓
Reglas (en orden de precedencia):
1. suj_exclusionSujeto = "E"  → true   (Sujeto Excluido)
2. suj_exclusionSujeto = "NE" → false  (Sujeto No Excluido)
3. soj_exclusionObjeto = "E"  → true   (Objeto Excluido)
4. soj_exclusionObjeto = "NE" → false  (Objeto No Excluido)
5. clasificacion = "1" && deuda30Objeto && tiene30ObjVinculo → true
6. clasificacion = "1" && deuda30Objeto && !tiene30ObjVinculo → false
7. clasificacion = "1" && !deuda30Objeto → false
8. clasificacion = "2" && suj_deuda30Sujeto → true
9. clasificacion = "2" && !suj_deuda30Sujeto → false
10. Default → true
```

**4. Resultado final:**
```scala
aplicarDescuento: Option[Boolean] = Some(result)
```

**5. Eventos persistidos:**
```scala
event1 = AplicarDescuentoUpdated(aplicarDescuento)
actor.state += event1

eventDmn = DmnResumen(
  objetoId, 
  sujetoId, 
  aplicarDescuento,
  dmnNumero,
  dmnDescripcion
)

// Persist a Kafka normal
actor.persistSnapshot(event, actor.state)

// Persist a Kafka resumen (solo si cambió y tipoObjeto in {A, I, N})
if (debeEnviarResumen(anterior, nuevo) && esTipoObjetoPermitido(tipoObjeto)) {
  actor.dmnresumenpersistSnapshot(eventDmn)
}
```

---

## 🎯 VARIABLES CLAVE EN CADA ENTIDAD

| Entidad | Variable | Significado |
|---------|----------|-------------|
| **Obligación** | `tiene30Obligaciones` | Si esta obligación específica NO es deuda (DMN result = 1) |
| **Objeto** | `tiene30Objeto` | Si TODAS las obligaciones del objeto NO son deuda |
| **ObjetoVinculo** | `tiene30ObjetoVinculo` | Si TODOS los cotitulares tienen `tiene30Objeto = true` |
| **Sujeto** | `tiene30Sujeto` | Si TODOS los objetos del sujeto tienen descuento (excluye clase 1) |
| **Objeto (final)** | `aplicarDescuento` | **RESULTADO FINAL** del DMN considerando TODAS las variables |

---
