# 📚 DOCUMENTACIÓN - Implementación de Responsabilidad Solidaria en Transferencias

---

## 📋 ÍNDICE

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Contexto del Problema](#contexto-del-problema)
3. [Solución Implementada](#solución-implementada)
4. [Arquitectura](#arquitectura)
5. [Flujo de Eventos](#flujo-de-eventos)
6. [Cambios Realizados](#cambios-realizados)
7. [Casos de Uso](#casos-de-uso)
8. [Validación](#validación)
9. [Consideraciones Futuras](#consideraciones-futuras)

---

## 🎯 RESUMEN EJECUTIVO

Se implementó un sistema de **responsabilidad solidaria en transferencias de objetos** (vehículos, inmuebles, etc.) que permite:

- ✅ **Heredar penalizaciones** cuando un nuevo titular adquiere un objeto con deuda preexistente
- ✅ **Liberar automáticamente** a todos los titulares cuando se paga la deuda
- ✅ **Mantener trazabilidad** de transferencias mediante el actor `ObjetoVinculo`
- ✅ **Propagar estados** consistentemente entre 3 agregados: Objeto, Sujeto y ObjetoVinculo

---

## 🔍 CONTEXTO DEL PROBLEMA

### **Problema Original:**

Cuando un objeto (ej: auto) se transfería de un titular viejo a uno nuevo:

1. ❌ El titular viejo conservaba la deuda
2. ❌ El titular nuevo NO heredaba la responsabilidad solidaria
3. ❌ Al pagar, solo se liberaba el titular viejo
4. ❌ El titular nuevo quedaba penalizado indefinidamente

### **Requerimiento de Negocio:**

Implementar **responsabilidad solidaria fiscal**: si compro un auto con deuda, heredo la penalización hasta que el titular anterior pague.

---

## ✅ SOLUCIÓN IMPLEMENTADA

### **Concepto Clave: Actor ObjetoVinculo**

Se utiliza un **actor intermediario** que mantiene dos mapas:

```scala
case class ObjetoVinculoState(
  mapTransf: Map[Vinculo, VinculoCotitular],    // Titulares anteriores (TRANSF)
  mapVinculo: Map[Vinculo, VinculoCotitular],   // Titulares actuales
  tiene30ObjetoVinculo: Boolean                 // ¿Hay deuda en transferencia?
)
```

### **Lógica de Responsabilidad Solidaria:**

```
tiene30ObjetoVinculo = mapTransf.forall(_.tiene30Objeto == true) 
                    && mapVinculo.forall(_.tiene30Objeto == true)
```

**Si un titular anterior tiene deuda (`mapTransf` con `false`):**
- ❌ `tiene30ObjetoVinculo = false`
- ❌ Se propaga a TODOS los vínculos en `mapVinculo` (titulares nuevos)

**Cuando se paga la deuda:**
- ✅ Se actualiza `mapTransf` a `true`
- ✅ `tiene30ObjetoVinculo` pasa a `true`
- ✅ Se liberan TODOS los titulares

---

## 🏗️ ARQUITECTURA

### **Agregados Involucrados:**

```
┌─────────────┐
│   Sujeto    │ (Persona/Empresa)
│             │
│ - tiene30   │ ← Se actualiza cuando cambian sus objetos
└──────┬──────┘
       │
       │ 1:N
       ▼
┌─────────────┐       ┌──────────────────┐
│   Objeto    │◄─────►│ ObjetoVinculo    │
│             │       │                  │
│ - tiene30   │       │ - mapTransf      │
│ - tiene30   │       │ - mapVinculo     │
│   Vinculo   │       │ - tiene30        │
│             │       │   ObjetoVinculo  │
└──────┬──────┘       └──────────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐
│ Obligación  │ (Deuda)
└─────────────┘
```

### **Patrón de Comunicación:**

```
Objeto ──Update──> ObjetoVinculo ──Calculate──> [mapTransf, mapVinculo]
                         │
                         └──Notify──> Objeto (tiene30ObjetoVinculo)
                                        │
                                        └──Update──> Sujeto (tiene30Sujeto)
```

---

## 📊 FLUJO DE EVENTOS

### **Escenario: Transferencia con Deuda**

#### **PASO 1-2-3: Setup Inicial**
```
1. Crear Sujeto Viejo (20-03034769-2)
2. Crear Objeto (AUTO09102) para Sujeto Viejo
3. Crear Obligación Vencida → Todas las marcas a FALSE
```

**Estado:**
```
mapVinculo: Map(Vinculo(Viejo) -> tiene30Objeto=false)
mapTransf: Map()
tiene30ObjetoVinculo: false
```

---

#### **PASO 4-5: Transferencia**
```
4. Crear Sujeto Nuevo (20-03034779-2)
5. Evento TRANSF del Sujeto Viejo (SOJ_ESTADO = "TRANSF")
```

**Evento procesado:**
```scala
case evt: CreatedTransfVinculoObjetoFromObj =>
  // Mover vínculo viejo a mapTransf
  val vinculosAnteriores = mapVinculo.filter(_.objetoId == AUTO09102)
  val _mapTransf = vinculosAnteriores + (Viejo -> tiene30Objeto=false)
  val _mapVinculo = Map(Viejo -> tiene30Objeto=false)  // Mantener en mapVinculo también
```

**Estado:**
```
mapTransf: Map(Vinculo(Viejo) -> tiene30Objeto=false)  ← Movido aquí
mapVinculo: Map(Vinculo(Viejo) -> tiene30Objeto=false)
tiene30ObjetoVinculo: false
```

---

#### **PASO 6: Nuevo Titular Adquiere**
```
6. Crear Objeto (AUTO09102) para Sujeto Nuevo (SOJ_ESTADO = "null")
```

**Evento procesado:**
```scala
case evt: UpdatedVinculoObjetoFromObj =>
  // Como NO es TRANSF, agregar a mapVinculo
  val _mapVinculo = mapVinculo + (Nuevo -> tiene30Objeto=false)
  
  // Propagar tiene30ObjetoVinculo a todos
  mapVinculo.foreach { vinculo =>
    send(UpdateState30ObjetoFromObjVinculo(
      tiene30ObjetoVinculo = false  ← Hereda penalización
    ))
  }
```

**Estado:**
```
mapTransf: Map(Vinculo(Viejo) -> tiene30Objeto=false)
mapVinculo: Map(
  Vinculo(Viejo) -> tiene30Objeto=false,
  Vinculo(Nuevo) -> tiene30Objeto=false  ← Heredó penalización
)
tiene30ObjetoVinculo: false
```

---

#### **PASO 7: PAGO (CRÍTICO)**
```
7. Pago de Obligación (RULE_NUMBER = "-1")
```

**Flujo completo:**

1️⃣ **ObligacionRemoveHandler:**
```scala
// Eliminar obligación
val event = ObjetoRemovedObligacion(...)
actor.state = actor.state.copy(
  obligaciones = obligaciones - obligacionId,
  tiene30Objeto = true  ← Ya no tiene deuda
)

// Enviar a ObjetoVinculo
SendObjetoToObjetoVinculo(
  estado = Some("TRANSF"),
  tiene30Objeto = true,
  command = ObjetoRemoveObligacion  ← Detecta que es PAGO
)
```

2️⃣ **SendObjetoToObjetoVinculo (FIX CRÍTICO):**
```scala
// ANTES: Siempre enviaba CreateTransfVinculoObjetoFromObj para TRANSF
// AHORA: Detecta si es PAGO
estado match {
  case Some("TRANSF") =>
    command match {
      case _: ObjetoRemoveObligacion =>
        // Es PAGO → Usar UPDATE (no CREATE)
        send(UpdateVinculoObjetoFromObj(...))  ✅
      case _ =>
        // Es transferencia real → Usar CREATE
        send(CreateTransfVinculoObjetoFromObj(...))
    }
}
```

3️⃣ **ObjetoVinculoState - UpdatedVinculoObjetoFromObj (FIX CRÍTICO):**
```scala
case evt: UpdatedVinculoObjetoFromObj =>
  val estaEnTransf = mapTransf.contains(vinculo)
  
  // ANTES: Solo actualizaba mapVinculo
  // AHORA: Actualiza AMBOS maps
  val _mapTransf = if (estaEnTransf) {
    mapTransf.updated(vinculo, tiene30Objeto=true)  ✅
  } else mapTransf
  
  val tiene30ObjetoVinculo = calcular30desdeMapVinculo(_mapVinculo, _mapTransf)
  // Resultado: true (ya no hay deuda en mapTransf)
```

4️⃣ **UpdateObjetoVinculoFromObjHandler (FIX CRÍTICO):**
```scala
// ANTES: Solo iteraba mapVinculo
// AHORA: Itera AMBOS maps
val todosLosVinculos = mapVinculo ++ mapTransf  ✅

todosLosVinculos.foreach { vinculo =>
  send(UpdateState30ObjetoFromObjVinculo(
    tiene30ObjetoVinculo = true  ← Libera a TODOS
  ))
}
```

5️⃣ **UpdateState30ObjetoFromObjVinculoHandler (FIX CRÍTICO):**
```scala
// ANTES: Solo heredaba penalización (false)
// AHORA: También LIBERA (true)
val tiene30ObjetoFinal = if (!tiene30ObjetoVinculo && obligaciones.isEmpty) {
  false  // Heredar penalización
} else if (tiene30ObjetoVinculo && obligaciones.isEmpty) {
  true   // LIBERAR ✅
} else {
  actor.state.tiene30Objeto
}

// CRÍTICO: Actualizar state ANTES de snapshot
actor.state = actor.state.copy(tiene30Objeto = tiene30ObjetoFinal)
actor.persistSnapshot(event, actor.state)  ← Proyecta correctamente

// Propagar a Sujeto
if (tiene30ObjetoFinal) {
  SendToSujeto1(...)  ← Handler que libera
}
```

**Estado Final:**
```
mapTransf: Map(Vinculo(Viejo) -> tiene30Objeto=true)  ✅
mapVinculo: Map(
  Vinculo(Viejo) -> tiene30Objeto=true,  ✅
  Vinculo(Nuevo) -> tiene30Objeto=true   ✅
)
tiene30ObjetoVinculo: true  ✅
```

---

## 🔧 CAMBIOS REALIZADOS

### **1. SendObjetoToObjetoVinculo.scala**

**Archivo:** `consumers/no_registral/objeto/application/helper/SendObjetoToObjetoVinculo.scala`

**Cambio:**
```scala
// Detectar si el comando es un PAGO
val esPago = command match {
  case _: ObjetoCommands.ObjetoRemoveObligacion => true
  case _ => false
}

estado match {
  case x if x.getOrElse("").equals("TRANSF") && !esPago =>
    // TRANSF real → CREATE
    send(CreateTransfVinculoObjetoFromObj(...))
    
  case x if x.getOrElse("").equals("TRANSF") && esPago =>
    // PAGO de objeto en TRANSF → UPDATE ✅
    send(UpdateVinculoObjetoFromObj(...))
    
  case _ =>
    // Otros casos → UPDATE
    send(UpdateVinculoObjetoFromObj(...))
}
```

**Propósito:** Evitar crear una transferencia duplicada cuando se paga una obligación.

---

### **2. ObjetoVinculoState.scala - UpdatedVinculoObjetoFromObj**

**Archivo:** `consumers/no_registral/tranferencia/domain/ObjetoVinculoState.scala`

**Cambio:**
```scala
case evt: UpdatedVinculoObjetoFromObj =>
  val estaEnVinculo = mapVinculo.contains(vinculo)
  val estaEnTransf = mapTransf.contains(vinculo)
  
  // Actualizar mapVinculo
  val _mapVinculo = if (estaEnVinculo) {
    UpdateObjVinculo(vinculo, vinculoCotitular)
  } else if (!estaEnTransf) {
    mapVinculo + (vinculo -> vinculoCotitular)  // Crear si no existe
  } else {
    mapVinculo
  }
  
  // NUEVO: Actualizar mapTransf también ✅
  val _mapTransf = if (estaEnTransf) {
    mapTransf.updated(vinculo, vinculoCotitular)
  } else {
    mapTransf
  }
  
  val _tiene30ObjetoVinculo = calcular30desdeMapVinculo(_mapVinculo, _mapTransf)
  
  copy(
    mapVinculo = _mapVinculo,
    mapTransf = _mapTransf,  // ← Se actualiza ahora
    tiene30ObjetoVinculo = _tiene30ObjetoVinculo
  )
```

**Propósito:** Permitir actualizar el estado de un vínculo tanto en `mapVinculo` como en `mapTransf`.

---

### **3. UpdateObjetoVinculoFromObjHandler.scala**

**Archivo:** `consumers/no_registral/tranferencia/application/cqrs/commands/UpdateObjetoVinculoFromObjHandler.scala`

**Cambio:**
```scala
// ANTES: Solo iteraba mapVinculo
actor.state.mapVinculo.foreach { vinculo => ... }

// AHORA: Itera AMBOS maps ✅
val todosLosVinculos = actor.state.mapVinculo ++ actor.state.mapTransf

todosLosVinculos.foreach { vinculo =>
  val tiene30Final = if (tieneDeuadEnTransf) false else tiene30ObjetoVinculo
  
  send(UpdateState30ObjetoFromObjVinculo(
    sujetoId = vinculo.sujetoId,
    objetoId = vinculo.objetoId,
    tiene30ObjetoVinculo = tiene30Final
  ))
}
```

**Propósito:** Notificar a TODOS los vínculos (actuales y anteriores) cuando cambia el estado de deuda.

---

### **4. UpdateState30ObjetoFromObjVinculoHandler.scala**

**Archivo:** `consumers/no_registral/objeto/application/cqrs/commands/UpdateState30ObjetoFromObjVinculoHandler.scala`

**Cambio:**
```scala
// NUEVA LÓGICA: Heredar Y Liberar
val tiene30ObjetoFinal = if (!tiene30ObjetoVinculo && obligaciones.isEmpty) {
  // Caso 1: Heredar penalización
  false
} else if (tiene30ObjetoVinculo && obligaciones.isEmpty) {
  // Caso 2: LIBERAR (NUEVO) ✅
  true
} else {
  // Caso 3: Tiene obligaciones propias
  actor.state.tiene30Objeto
}

// CRÍTICO: Actualizar state ANTES de persistir ✅
actor.state = actor.state.copy(tiene30Objeto = tiene30ObjetoFinal)

actor.persistSnapshot(event, actor.state) { () =>
  if (tiene30ObjetoFinal) {
    SendToSujeto1(...)  // Handler que libera al sujeto
  } else {
    SendToSujeto(...)   // Handler que penaliza al sujeto
  }
}
```

**Propósito:** Implementar la lógica de liberación y actualizar correctamente el state antes de proyectar.

---

### **5. ObjetoRemoveObligacionHandler.scala**

**Archivo:** `consumers/no_registral/objeto/application/cqrs/commands/ObjetoRemoveObligacionHandler.scala`

**Cambio:**
```scala
actor.persistEvent(event) { () =>
  actor.state += event
  
  // NUEVO: Persistir snapshot ANTES de enviar a ObjetoVinculo ✅
  actor.persistSnapshot(event, actor.state) { () =>
    
    if (!actor.state.isBaja && actor.state.registro.isDefined) {
      SendObjetoToObjetoVinculo(...)
    }
    
    sender ! Response.SuccessProcessing(...)
  }
}
```

**Propósito:** Garantizar que el snapshot se persiste antes de propagar el cambio.

---

## 📝 CASOS DE USO

### **Caso 1: Transferencia sin Deuda**
```
1. Sujeto A tiene auto sin deuda
2. Sujeto B compra el auto
3. Resultado: Sujeto B NO hereda penalización (no hay deuda)
```

### **Caso 2: Transferencia con Deuda (Implementado)**
```
1. Sujeto A tiene auto CON deuda vencida
2. Sujeto B compra el auto
3. Resultado: Sujeto B HEREDA penalización
4. Sujeto A paga la deuda
5. Resultado: AMBOS sujetos se liberan ✅
```

### **Caso 3: Múltiples Cotitulares**
```
1. Sujeto A y B son cotitulares de un auto con deuda
2. Se transfiere a Sujeto C
3. Resultado: A y B se mueven a mapTransf, C hereda penalización
4. A paga la deuda
5. Resultado: A, B y C se liberan ✅
```

### **Caso 4: Orden de Eventos Flexible**
```
Opción A: TRANSF → Nuevo Titular → Pago ✅
Opción B: Nuevo Titular → TRANSF → Pago ✅

Ambos órdenes funcionan correctamente.
```

---

## ✅ VALIDACIÓN

### **Queries de Verificación:**

```sql
-- 1. Estado del Actor Vínculo
SELECT soj_identificador, soj_map_transf, soj_map_vinculo, soj_tiene30objetovinculo
FROM buc_objeto_vinculo
WHERE soj_identificador = 'AUTO09102';

-- 2. Estado de los Objetos
SELECT soj_suj_identificador, soj_identificador, 
       soj_tiene30objeto, soj_tiene30objetovinculo, soj_aplicardescuento
FROM buc_sujeto_objeto
WHERE soj_identificador = 'AUTO09102';

-- 3. Estado de los Sujetos
SELECT suj_identificador, suj_tiene30sujeto
FROM buc_sujeto
WHERE suj_identificador IN ('20-03034769-2', '20-03034779-2');

-- 4. Obligaciones (debe estar vacío después del pago)
SELECT * FROM buc_obligaciones 
WHERE bob_obn_id = '20250000000005413851';
```

### **Resultado Esperado Después del Pago:**

```
✅ Actor Vínculo:
   - mapTransf: Map(Vinculo(Viejo) -> tiene30Objeto=true)
   - mapVinculo: Map(Vinculo(Viejo,...), Vinculo(Nuevo,...))
   - tiene30ObjetoVinculo: true

✅ Objetos (AMBOS):
   - soj_tiene30objeto: true
   - soj_tiene30objetovinculo: true
   - soj_aplicardescuento: true (Nuevo) / false (Viejo)*

✅ Sujetos (AMBOS):
   - suj_tiene30sujeto: true

✅ Obligaciones:
   - (0 rows) - Eliminada correctamente
```

**\*Nota:** El campo `soj_aplicardescuento` del sujeto viejo queda en `false` porque el objeto ya no le pertenece (está en estado TRANSF). Esto es el comportamiento esperado y no afecta la funcionalidad.

---

## 🔮 CONSIDERACIONES FUTURAS

### **Mejoras Recomendadas:**

1. **Limpieza de `mapTransf`:**
    - Actualmente, los vínculos anteriores permanecen indefinidamente en `mapTransf`
    - Implementar lógica de purga después de N días/meses

2. **Múltiples Transferencias Consecutivas:**
    - Validar comportamiento con cadenas largas: A → B → C → D

3. **Testing Automatizado:**
    - Crear test suite con Akka TestKit
    - Casos edge: pagos parciales, transferencias concurrentes, reintentos

4. **Métricas y Monitoring:**
    - Tamaño de `mapTransf` y `mapVinculo`
    - Tiempo de propagación de eventos
    - Rate de eventos `UpdateState30ObjetoFromObjVinculo`

5. **Optimización de Snapshots:**
    - Evaluar si se pueden reducir snapshots duplicados
    - Implementar estrategia de compresión para `mapTransf` grandes

    

---

**Fin del Documento** ✅

---

