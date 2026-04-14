# Análisis Arquitectónico Completo de Copernico2

> Generado el 14/04/2026

---

## 1. Visión General del Sistema

**Copernico2** es un sistema de clasificación de personas tributarias (PCS = *Person Classification Service*) que implementa una arquitectura orientada a eventos con los siguientes patrones:

- **Event Sourcing** con Akka Persistence
- **CQRS** (Command Query Responsibility Segregation)
- **DDD** (Domain-Driven Design) con Aggregates, Commands, Events y States
- **Actor Model** de Akka (tanto Classic como Typed)
- **Cluster Sharding** de Akka para escalabilidad horizontal
- **Event-Driven** via Apache Kafka

El sistema tiene **dos módulos principales** que se ejecutan como microservicios independientes:
- **`pcs/`** – El "write side": procesa comandos y genera eventos
- **`readside/`** – El "read side": proyecta eventos en Cassandra para consultas

---

## 2. Punto de Entrada

### PCS Write Side — `pcs/src/main/scala/Main.scala`

```scala
object Main extends App {
  val config = ConfigFactory.load()
  val actorSystemName = "PersonClassificationService"
  startMicroservices(microservices, ip, port, actorSystemName)
```

Este `Main` invoca `startMicroservices` con una lista de ~20 microservicios Kafka. Cada uno consume un tópico específico:

- `SujetoMicroservice` → tópicos: `DGR-COP-SUJETOS-TRI`, `DGR-COP-SUJETOS-ANT`
- `ObjetoMicroservice` → tópicos: `DGR-COP-OBJETOS-TRI`, `DGR-COP-OBJETOS-ANT`, etc.
- `ObligacionMicroservice` → tópicos: `DGR-COP-OBLIGACIONES-TRI`, etc.
- Más ~17 microservicios "registrales" (juicios, planes de pago, trámites, etc.)

### ReadSide — `readside/src/main/scala/readside/Main.scala`

```scala
object Main extends App {
  val actorSystemName = "PersonClassificationServiceReadSide"
  startMicroservices(microservices, ip, port, actorSystemName)
```

Consume tópicos de "snapshots" producidos por el write side (e.g., `ObjetoSnapshotPersistedReadside`, `SujetoSnapshotPersisted`, `ObligacionPersistedSnapshot`).

---

## 3. Arranque del Sistema — `MainApplication`

`common/src/main/scala/design_principles/microservice/kafka_consumer_microservice/MainApplication.scala`

```scala
object MainApplication {
  def startMicroservices(...): Unit = {
    implicit val system: ActorSystem = Guardian.getContext(...)
    val routes = ProductionMicroserviceContextProvider.getContext(...) { implicit microserviceProvisioning =>
      val microservices = microservicesFactory(microserviceProvisioning)
      // ... expone routes HTTP
    }
    AkkaHttpServer.start(routes, ip, port)(system)
    Await.result(system.whenTerminated, Duration.Inf)
  }
}
```

Se crea un **ActorSystem** de Akka, se instancian todos los microservicios con sus dependencias (Kafka consumer/producer, Cassandra, monitoring) y se levanta un servidor HTTP con Akka HTTP.

---

## 4. Flujo Completo Paso a Paso

### PASO 1: Llegada del mensaje desde Kafka

Cada microservicio tiene uno o más `ActorTransaction[T]` que representan una "transacción" sobre un tópico Kafka. Por ejemplo:

**`ObjetoTributarioTransaction`** (`pcs/src/main/scala/consumers/no_registral/objeto/infrastructure/consumer/ObjetoTributarioTransaction.scala`):

```scala
def topic = "DGR-COP-OBJETOS-TRI"
def topicRetry = "DGR-COP-OBJETOS-TRI_retry"
def topicError = "DGR-COP-OBJETOS-TRI_error"
```

El `ActorTransactionController` inicia el stream Kafka (`ActorTransactionController.scala`, línea 103):

```scala
val (killSwitch, done) = new KafkaCommittablePartitionedMessageProcessor(requirements)
  .run(topic, s"${topic}SINK", topicRetry, topicError, message => {
    transaction(message).map { output => Seq(output.toString) }
  })
```

El stream usa **Alpakka Kafka** con "committable partitioned source" (procesa particiones en paralelo, hace commit de offsets explícitamente).

---

### PASO 2: Deserialización del mensaje

```scala
// ActorTransaction.scala, línea 24-45
final def transaction(input: String): Future[Response.SuccessProcessing] = {
  processInput(input) match {
    case Left(serializationError) => Future.failed(serializationError)  // → tópico error
    case Right(value) => processMessage(value)  // → lógica de negocio
  }
}
```

En `ObjetoTributarioTransaction`:

```scala
def processInput(input: String): Either[Throwable, ObjetosTri] =
  decode[ObjetosTri](input)  // circe JSON deserialization
```

---

### PASO 3: Construcción del Comando

```scala
// ObjetoTributarioTransaction.scala, líneas 88-118
val command: ObjetoCommands =
  if (registro.SOJ_ESTADO.contains("BAJA"))
    ObjetoCommands.SetBajaObjeto(sujetoId, objetoId, tipoObjeto, ...)
  else
    ObjetoCommands.ObjetoUpdateFromTri(sujetoId, objetoId, tipoObjeto, ...)
```

El comando se envía al **Actor** mediante `ask` (pattern `?`):

```scala
actorRef.ask[Response.SuccessProcessing](command)
// o, si BETTER_SORTER_OBJETO_TRI=ON:
commandRouter.ask[Response.SuccessProcessing](command)
```

---

### PASO 4: Routing hacia el Actor correcto (Cluster Sharding)

El `SujetoActor` y `ObjetoActor` están registrados como **Sharded Entities** (`ShardedEntity[MonitoringAndMessageProducer]`):

```scala
// ShardedEntity.scala, líneas 35-44
def startWithRequirements(requirements: Requirements)(implicit system: ActorSystem): ActorRef =
  ClusterSharding(system).start(
    typeName = typeName,
    entityProps = props(requirements),
    extractEntityId = extractEntityId,  // entityId = "Sujeto-<sujetoId>"
    extractShardId = extractShardId     // shardId = hash(entityId) % NR_PARTITIONS
  )
```

El número de particiones por defecto es 90 (`NR_PARTITIONS`). El mensaje se dirige al shard correcto por el `shardedId` del comando (e.g., `Sujeto-12345`).

---

### PASO 5: Jerarquía de Actores (Árbol de Actores)

El sistema usa una **jerarquía de actores padre-hijo** (no sharding para niveles intermedios):

```
ShardRegion (Cluster Sharding)
  └── SujetoActor (PersistentActor) [id: "Sujeto-<sujetoId>"]
        └── ObjetoActor (PersistentActor) [id: "Sujeto-<sujetoId>-Objeto-<objetoId>-<tipoObjeto>"]
              └── ObligacionActor (PersistentActor) [id: "Sujeto-<sujetoId>-Objeto-<objetoId>-<tipoObjeto>-Obligacion-<obligacionId>"]
```

- El `SujetoActor` crea `ObjetoActor`s como hijos cuando los necesita (`context.actorOf(...)`)
- El `ObjetoActor` crea `ObligacionActor`s como hijos
- Los mensajes de `ObjetoMessage` que llegan al `SujetoActor` son reenviados (`forward`) al hijo correspondiente (`SujetoActor.scala`, línea 68)

---

### PASO 6: Recepción del Comando en el Actor

```scala
// PersistentBaseActor.scala, líneas 28-35
override def receiveCommand: Receive = {
  case cmd: Command => commandBus.publish(cmd)
  case query: Query => queryBus.ask(query)
  case other => logger.warn(...)
}
```

El **CommandBus** usa subscriptores registrados en `setupHandlers()`:

```scala
// ObjetoActor.scala, líneas 33-65
override def setupHandlers(): Unit = {
  commandBus.subscribe[ObjetoCommands.ObjetoUpdateFromTri](
    new ObjetoUpdateFromTriHandler(this, requirements).handle
  )
  // ... otros handlers
}
```

---

### PASO 7: Procesamiento del Comando en el Handler

El `ObjetoUpdateFromTriHandler` (`pcs/src/main/scala/consumers/no_registral/objeto/application/cqrs/commands/ObjetoUpdateFromTriHandler.scala`):

1. **Verifica idempotencia**: Si `command.deliveryId <= actor.state.lastDeliveryIdByEvents`, responde `IDEM-<aggregateRoot>` sin procesar
2. **Calcula clasificación** via **DMN** (`DMNTreintaPorcientoTipo.calcularDmn(...)`)
3. **Crea el evento** `ObjetoEvents.ObjetoUpdatedFromTri`
4. **Persiste el evento** con `actor.persistEvent(event) { () => ... }`

---

### PASO 8: Persistencia del Evento (Event Sourcing)

```scala
// PersistentBaseActor.scala, líneas 67-74
def persistEvent(event: E, tags: Set[String] = Set.empty)(handler: () => Unit = () => ()): Unit = {
  persistAsync(event) { _ =>
    // callback después de persistir en el journal (Cassandra)
    handler()
  }
}
```

Los eventos se persisten en el **journal de Akka Persistence** (Cassandra). Esto garantiza que el estado pueda reconstruirse ante reinicios.

---

### PASO 9: Actualización del State

```scala
// ObjetoUpdateFromTriHandler.scala, línea 172
actor.persistEvent(event) { () =>
  actor.state += event  // actualiza el estado en memoria
  // ...
}
```

El operador `+=` llama a `ObjetoState.+(event: ObjetoEvents)`:

```scala
// ObjetoState.scala, línea 50-70
override def +(event: ObjetoEvents): ObjetoState = {
  eventCounter match {
    case n if (n > eventCounterMax) => changeState(event).copy(eventCounter = 0, ...)
    case n => changeState(event).copy(eventCounter = n + 1, ...)
  }
}
```

`changeState` contiene la lógica de negocio para cada tipo de evento (e.g., actualizar saldo, set de sujetos, clasificación, etc.).

---

### PASO 10: Propagación Ascendente / Comunicación entre Aggregates

Después de actualizar el estado del `ObjetoActor`, se notifica al padre (`SujetoActor`):

```scala
// ObjetoUpdateFromTriHandler.scala, líneas 176-225
actor.context.parent ! SujetoCommands.SujetoUpdateFromObjeto(
  command.deliveryId,
  command.sujetoId, command.objetoId, command.tipoObjeto,
  actor.state.saldo,
  actor.state.obligacionesSaldo.values.sum,
  actor.state.clasificacionObjeto, ...
)
```

O para objetos tipo "M" con `tiene30Objeto = false`:

```scala
actor.context.parent ! SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento(...)
```

El `SujetoUpdateFromObjetoHandler` persiste a su vez un `SujetoEvents.SujetoUpdatedFromObjeto` y calcula `tiene30Sujeto` (¿tiene deuda en los últimos 30 días?).

Después, si el estado del sujeto cambió (`diffStates = true`), **notifica a los objetos hijo** (`SendToObjeto.scala`):

```scala
// SendToObjeto.scala, línea 24-35
objChild.ask[Response.SuccessProcessing](
  ObjetoUpdateFromSujeto(
    tiene30Sujeto = currentState.tiene30Sujeto,
    exclusionSUjeto = currentState.exclusionSujeto, ...
  )
)
```

---

### PASO 11: Propagación Descendente — Obligaciones

Para las obligaciones tributarias, el `ObjetoActor` recibe mensajes de tipo `ObligacionMessage` y los reenvía al `ObligacionActor` hijo:

```scala
// ObjetoActor.scala, líneas 118-140
def processObligacionMessages: Receive = {
  case childMessage: ObligacionMessage =>
    val obligacion = obligaciones((sujetoId, objetoId, tipoObjeto, obligacionId))
    obligacion forward childMessage
    // Si es ObligacionUpdateFromDto, también aplica exenciones al hijo
}
```

El `ObligacionActor` procesa el comando, persiste el evento `ObligacionUpdatedFromDto` y notifica al padre objeto:

```scala
// ObligacionActor.scala, líneas 38-65
def informParent(cmd: ObligacionCommands): Unit = {
  context.parent ! ObjetoCommands.ObjetoUpdateFromObligacion(
    cmd.deliveryId, saldo, exenta, porcentajeExencion, ...
  )
}
// O para obligaciones con 30 días de deuda:
def informParentTreintaProciento(evt: ObligacionUpdatedFromDto): Unit = {
  context.parent ! ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento(...)
}
```

---

### PASO 12: Publicación del Snapshot a Kafka (Puente Write→Read)

Después de persistir el evento, los actores producen un **snapshot** al tópico Kafka correspondiente.

**ObjetoActor** (`ObjetoActor.scala`, líneas 142-186):

```scala
def persistSnapshot(evt: ObjetoEvents, consolidatedState: ObjetoState)(handler: () => Unit): Unit = {
  val snapshot = ObjetoSnapshotPersisted(
    saldo = consolidatedState.saldo,
    cotitulares = consolidatedState.sujetos,
    obligacionesSaldo = consolidatedState.obligacionesSaldo,
    tiene30Objeto = Some(consolidatedState.tiene30Objeto), ...
  )
  requirements.messageProducer.produce(
    data = Seq(KafkaKeyValue(snapshot.aggregateRoot, snapshot.asJson.toString())),
    topic = "ObjetoSnapshotPersistedReadside"
  ) { _ => handler() }  // el handler envía la respuesta al sender
}
```

**SujetoActor** (`SujetoActor.scala`, líneas 87-102):

```scala
def persistSnapshot()(handler: Seq[KafkaKeyValue] => Unit): Unit = {
  requirements.messageProducer.produce(
    data = Seq(KafkaKeyValue(persistenceId, event)),
    topic = "SujetoSnapshotPersisted"
  )(handler)
}
```

**ObligacionActor** produce a `ObligacionPersistedSnapshot`.

La llamada `handler()` ocurre **solo después** de que Kafka confirma la publicación, y dentro del `handler` se envía `Response.SuccessProcessing` al sender original, lo que hace que se commitee el offset de Kafka.

---

### PASO 13: Read Side — Proyección en Cassandra

El **ReadSide** consume estos tópicos de snapshot. Por ejemplo, `ObjetoSnapshotPersistedHandler` (`readside/src/main/scala/readside/proyectionists/no_registrales/objeto/ObjetoSnapshotPersistedHandler.scala`):

```scala
override def topic: String = "ObjetoSnapshotPersistedReadside"

override def processMessage(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
  val projection = ObjetoSnapshotPersistedProjection(registro)
  r.cassandraWrite.writeState(projection)  // persiste en Cassandra
}
```

`ObjetoSnapshotPersistedProjection` extiende `UpdateReadSideProjection` que genera un `UPDATE` CQL:

```scala
// UpdateReadSideProjection.scala, línea 48-53
def statement: String = {
  s"""UPDATE $collectionName
     | SET ${sets(curatedBindings)}
     | WHERE ${setsKeys(curatedKeys)}
  """.stripMargin
}
// collectionName = "read_side.buc_sujeto_objeto"
// keys = soj_suj_identificador, soj_tipo_objeto, soj_identificador
```

Hay proyecciones especializadas:
- `ObjetoPatenteProjection` → tabla `buc_objeto_patente`
- `ObjetoDocumentoProjection` → tabla `buc_objeto_documento`

(Solo para tipos de objeto `CAM` y `NAUT`.)

---

## 5. Diagrama de Flujo Completo

```
KAFKA (tópicos externos)                           WRITE SIDE (pcs)
─────────────────────                              ────────────────
DGR-COP-OBJETOS-TRI  ──► KafkaCommittable         ┌─────────────────────────────────────┐
DGR-COP-SUJETOS-TRI  ──► PartitionedMessage  ──►  │ ActorTransaction.transaction(input) │
DGR-COP-OBLIG-TRI    ──► Processor                │  ├─ processInput() → deserializa    │
                                                   │  └─ processMessage() → crea Command│
                                                   └──────────────┬──────────────────────┘
                                                                  │ ask(command)
                                                   ┌──────────────▼──────────────────────┐
                                                   │  Cluster Sharding (ShardRegion)     │
                                                   │  entityId → shardId hash(id)%90    │
                                                   └──────────────┬──────────────────────┘
                                                                  │ forward
                                                   ┌──────────────▼──────────────────────┐
                                                   │ SujetoActor (PersistentActor)       │
                                                   │  state: SujetoState                 │
                                                   │  commandBus.publish(cmd)            │
                                                   │  → SujetoUpdateFromTriHandler:      │
                                                   │    1. Check idempotencia            │
                                                   │    2. Crea SujetoEvents.Xxx         │
                                                   │    3. persistEvent(event)           │
                                                   │       → Cassandra Journal           │
                                                   │    4. state += event                │
                                                   │    5. persistSnapshot()             │
                                                   │       → Kafka:"SujetoSnapshotPer.." │
                                                   │    6. sender ! SuccessProcessing    │
                                                   │       → Kafka commit offset         │
                                                   └──────────┬──────┬───────────────────┘
                                              forward msgs    │      │ ! SujetoUpdateFromObjeto
                                              (ObjectMsg)     │      ▼
                                                   ┌──────────▼──────────────────────────┐
                                                   │ ObjetoActor (PersistentActor)       │
                                                   │  state: ObjetoState                 │
                                                   │  → ObjetoUpdateFromTriHandler:      │
                                                   │    1. Check idempotencia            │
                                                   │    2. DMN clasificación (Camunda)   │
                                                   │    3. Crea ObjetoEvents.Xxx         │
                                                   │    4. persistEvent(event)           │
                                                   │       → Cassandra Journal           │
                                                   │    5. state += event                │
                                                   │    6. context.parent ! SujetoCmd   │
                                                   │    7. persistSnapshot()             │
                                                   │       → Kafka:"ObjetoSnapshot.."   │
                                                   │    8. SendObjetoToObjetoVinculo    │
                                                   └──────────┬──────────────────────────┘
                                              forward msgs    │
                                              (ObligMsg)      │
                                                   ┌──────────▼──────────────────────────┐
                                                   │ ObligacionActor (PersistentActor)   │
                                                   │  state: ObligacionState             │
                                                   │  → ObligUpdateFromDtoHandler:       │
                                                   │    1. Crea ObligacionEvents.Xxx     │
                                                   │    2. persistEvent(event)           │
                                                   │       → Cassandra Journal           │
                                                   │    3. state += event                │
                                                   │    4. context.parent ! ObjCmd      │
                                                   │       (ObjetoUpdateFromObligacion)  │
                                                   │    5. persistSnapshot()             │
                                                   │       → Kafka:"ObligacionPersist.." │
                                                   └──────────────────────────────────────┘

KAFKA (tópicos internos/snapshots)                 READ SIDE (readside)
──────────────────────────────                     ────────────────────
ObjetoSnapshotPersistedReadside  ──────────────►  ObjetoSnapshotPersistedHandler
SujetoSnapshotPersisted          ──────────────►  SujetoSnapshotPersistedHandler
ObligacionPersistedSnapshot      ──────────────►  ObligacionPersistedSnapshotHandler
                                                          │
                                                   ┌──────▼──────────────────────────────┐
                                                   │ CassandraWriteProduction.writeState()│
                                                   │  UPDATE read_side.buc_sujeto_objeto │
                                                   │  UPDATE read_side.buc_obligaciones  │
                                                   │  UPDATE read_side.buc_sujeto        │
                                                   └──────────────────────────────────────┘
                                                                  │
                                                              CASSANDRA
                                                        (read_side keyspace)
```

---

## 6. Componentes Principales

| Componente | Tipo | Archivo | Descripción |
|---|---|---|---|
| `Main` (pcs) | Entry Point | `pcs/src/main/scala/Main.scala` | Inicia ~20 microservicios Kafka |
| `Main` (readside) | Entry Point | `readside/src/main/scala/readside/Main.scala` | Inicia microservicios de proyección |
| `MainApplication` | Bootstrap | `common/.../MainApplication.scala` | Crea ActorSystem, HTTP server |
| `KafkaConsumerMicroservice` | Abstract | `common/.../KafkaConsumerMicroservice.scala` | Base para cada microservicio Kafka |
| `ActorTransaction[T]` | Abstract | `common/.../ActorTransaction.scala` | Conecta stream Kafka ↔ Actor |
| `ActorTransactionController` | Controller | `common/.../ActorTransactionController.scala` | Gestiona el KillSwitch del stream Kafka |
| `KafkaCommittablePartitionedMessageProcessor` | Infrastructure | `common/.../Kafka*.scala` | Implementación del stream Akka-Kafka |
| `SujetoActor` | Aggregate Root | `pcs/.../SujetoActor.scala` | Actor padre, gestiona el Sujeto tributario |
| `ObjetoActor` | Aggregate | `pcs/.../ObjetoActor.scala` | Actor hijo, gestiona el Objeto (bien) |
| `ObligacionActor` | Aggregate | `pcs/.../ObligacionActor.scala` | Actor nieto, gestiona la Obligación tributaria |
| `PersistentBaseActor[E,S]` | Base | `common/.../PersistentBaseActor.scala` | Base para actores con Event Sourcing |
| `CommandHandler[P,C]` | Pattern | `common/.../CommandHandler.scala` | Interfaz para handlers de comandos |
| `ObjetoUpdateFromTriHandler` | Handler | `pcs/.../ObjetoUpdateFromTriHandler.scala` | Handler para actualizar objeto desde TRI |
| `ObligacionUpdateFromDtoHandler` | Handler | `pcs/.../ObligacionUpdateFromDtoHandler.scala` | Handler para actualizar obligación |
| `ShardedEntity` | Infrastructure | `common/.../ShardedEntity.scala` | Cluster Sharding con Akka |
| `ObjetoCommandRouter` | Router | `pcs/.../ObjetoCommandRouter.scala` | Router opcional por objetoId para serializar comandos |
| `KafkaMessageProducer` | Infrastructure | `common/.../KafkaMessageProducer.scala` | Publica snapshots a Kafka |
| `UpdateReadSideProjection[E]` | Pattern | `common/.../UpdateReadSideProjection.scala` | Genera CQL UPDATE para Cassandra |
| `CassandraWriteProduction` | Infrastructure | `common/.../CassandraWriteProduction.scala` | Ejecuta CQL en Cassandra |
| `ObjetoSnapshotPersistedHandler` | Projector | `readside/.../ObjetoSnapshotPersistedHandler.scala` | Proyecta snapshots de Objeto en Cassandra |
| `SujetoSnapshotPersistedHandler` | Projector | `readside/.../SujetoSnapshotPersistedHandler.scala` | Proyecta snapshots de Sujeto en Cassandra |
| `DMNTreintaPorcientoTipo` | Business Logic | `pcs/.../DMNTreintaPorcientoTipo.scala` | Reglas DMN (Decision Model Notation) de Camunda |

---

## 7. Patrones de Arquitectura Detectados

### Event Sourcing

Los actores son `PersistentActor` (Akka Persistence). Cada cambio de estado se persiste como un evento en el journal de Cassandra. Al reiniciar, los actores se reconstruyen reproduciendo todos los eventos (`receiveRecover`).

### CQRS

- **Write Side** (`pcs`): recibe comandos (`Command`), produce eventos (`Event`), mantiene el estado con toda la lógica de negocio
- **Read Side** (`readside`): consume eventos/snapshots del Write Side y proyecta en tablas Cassandra optimizadas para lectura

### DDD (Domain-Driven Design)

- **Aggregates**: `SujetoActor`, `ObjetoActor`, `ObligacionActor` — cada uno tiene su `State` y sus `Events`
- **Commands**: `ObjetoCommands.ObjetoUpdateFromTri`, `SujetoCommands.SujetoUpdateFromObjeto`, etc.
- **Events**: `ObjetoEvents.ObjetoUpdatedFromTri`, `SujetoEvents.SujetoUpdatedFromObjeto`, etc.
- **Bounded Contexts**: `no_registral` (datos propios del PCS) vs `registral` (datos de sistemas externos)

### Actor Model (Akka)

- Jerarquía de actores Sujeto→Objeto→Obligación que mapea el modelo de dominio
- Cluster Sharding para distribución horizontal por `sujetoId`
- Comunicación asíncrona vía mensajes (`!` tell, `?` ask, `forward`)
- Supervisión implícita (actor padre supervisa hijos)

### Event-Driven Architecture con Kafka

- Decoupling total entre Write Side y Read Side vía tópicos Kafka
- At-least-once delivery con commit manual de offsets
- Circuit breaker implícito: si el procesamiento falla, el mensaje va al tópico `_retry` o `_error`

### Idempotencia por Delivery ID

```scala
// DeliveryIdManagement
def isIdempotent(command, lastDeliveryId): Boolean =
  command.deliveryId <= lastDeliveryId
```

Cada mensaje tiene un `EV_ID` (delivery ID). Los actores rechazan comandos con ID menor o igual al último procesado, retornando `IDEM-<aggregateRoot>`.

### State Parcial (Partial State Pattern)

Configurable vía `STATE_PARCIAL_OBJETO_TRI=ON/OFF`. Cuando está activo, solo sobreescribe campos no-nulos del DTO entrante usando el estado actual como base (`StateParcialObjeto.stateParcialCC`).

### Snapshots periódicos

Cada `eventCounterMax` eventos, el actor guarda un snapshot de Akka Persistence y elimina eventos anteriores, para acelerar la recuperación.

---

## 8. Persistencia Dual

El sistema usa **dos mecanismos de persistencia**:

1. **Cassandra Journal** (Akka Persistence): almacena todos los eventos del dominio para reconstruir estado de actores. Esta es la fuente de verdad del Write Side.

2. **Cassandra Read Side** (tablas `read_side.*`): estado proyectado, desnormalizado y optimizado para queries. El Read Side escribe via `UPDATE ... SET ... WHERE ...` sin necesidad de conocer el estado previo.

Los tópicos Kafka actúan como **bus de integración** entre ambos mundos.

---

## 9. Flujo End-to-End — Ejemplo Concreto

**Caso**: Llega un objeto tributario nuevo al sistema.

| Paso | Componente | Acción |
|---|---|---|
| 1 | Kafka | Produce mensaje JSON en `DGR-COP-OBJETOS-TRI` |
| 2 | `KafkaCommittablePartitionedMessageProcessor` | Consume el mensaje (sin commit aún) |
| 3 | `ObjetoTributarioTransaction.processInput()` | Deserializa JSON → `ObjetosTri` |
| 4 | `ObjetoTributarioTransaction.processMessage()` | Construye `ObjetoCommands.ObjetoUpdateFromTri` |
| 5 | Cluster Sharding | Dirige al shard del `sujetoId` |
| 6 | `SujetoActor` | Recibe como `ObjetoMessage` → reenvía (`forward`) al `ObjetoActor` hijo |
| 7 | `ObjetoActor` → `commandBus` | Llama a `ObjetoUpdateFromTriHandler.handle()` |
| 8 | `ObjetoUpdateFromTriHandler` | Calcula DMN, crea evento `ObjetoUpdatedFromTri` |
| 9 | `persistEvent(event)` | Evento guardado en **Cassandra Journal** |
| 10 | `state += event` | `ObjetoState` actualizado en memoria |
| 11 | `context.parent ! SujetoCommands.SujetoUpdateFromObjeto(...)` | Notifica al `SujetoActor` |
| 12 | `SujetoActor` | Persiste `SujetoUpdatedFromObjeto` → actualiza `SujetoState` → recalcula `tiene30Sujeto` |
| 13 | `SendToObjeto` (si `diffStates = true`) | Manda `ObjetoUpdateFromSujeto` a todos los objetos hijo |
| 14 | `ObjetoActor.persistSnapshot()` | Publica `ObjetoSnapshotPersisted` a **Kafka** `ObjetoSnapshotPersistedReadside` |
| 15 | `SujetoActor.persistSnapshot()` | Publica `SujetoSnapshotPersisted` a **Kafka** |
| 16 | Kafka confirma publicación | Se envía `Response.SuccessProcessing` → stream commitea offset |
| 17 | ReadSide: `ObjetoSnapshotPersistedHandler` | Consume tópico → `ObjetoSnapshotPersistedProjection` |
| 18 | `CassandraWriteProduction.writeState()` | `UPDATE read_side.buc_sujeto_objeto SET ... WHERE ...` |
| 19 | Cassandra confirma | ReadSide commitea offset en su tópico |

---

## 10. Variables de Entorno Clave

| Variable | Valores | Descripción |
|---|---|---|
| `NR_PARTITIONS` | número (default: 90) | Particiones de Cluster Sharding |
| `BETTER_SORTER_OBJETO_TRI` | `ON`/`OFF` | Activa el `ObjetoCommandRouter` para serializar comandos por objetoId |
| `STATE_PARCIAL_OBJETO_TRI` | `ON`/`OFF` | Activa el patrón de state parcial en objetos TRI |
| `STATE_PARCIAL_OBLIGACION_TRI` | `ON`/`OFF` | Activa el patrón de state parcial en obligaciones TRI |
| `MULTI_OBJETO_ANT` | `ON`/`OFF` | Proyecta objetos tipo CAM/NAUT en tablas extra de patente y documento |
| `MESSAGE_BEHIND_1_ENABLED` | `ON`/`OFF` | Habilita métricas de lag de Kafka |
| `MESSAGE_BEHIND_INTERVAL_SECONDS` | número (default: 60) | Intervalo de reporte de lag |
| `KAFKA_BROKERS_LIST_PSRM` | lista de brokers | Brokers del producer PSRM (alternativo) |
| `INITIALIZATION` | string | Modo de inicialización especial |
| `ENABLE_TRAZ` | string | Habilita trazabilidad en ObligacionActor |
| `CASSANDRA_METRICS_PORT` | puerto (default: 9089) | Puerto de métricas Cassandra |

---

## 11. Tópicos Kafka

### Tópicos de Entrada (Write Side consume)

| Tópico | Microservicio | Tipo de Dato |
|---|---|---|
| `DGR-COP-SUJETOS-TRI` | `SujetoMicroservice` | `SujetoExternalDto` |
| `DGR-COP-SUJETOS-ANT` | `SujetoMicroservice` | `SujetoExternalDto` |
| `DGR-COP-OBJETOS-TRI` | `ObjetoMicroservice` | `ObjetosTri` |
| `DGR-COP-OBJETOS-ANT` | `ObjetoMicroservice` | `ObjetosAnt` |
| `DGR-COP-OBLIGACIONES-TRI` | `ObligacionMicroservice` | `ObligacionExternalDto` |
| `DGR-COP-OBLIGACIONES-ANT` | `ObligacionMicroservice` | `ObligacionExternalDto` |
| `DGR-COP-JUICIOS` | `JuicioMicroservice` | — |
| `DGR-COP-TRAMITES` | `TramiteMicroservice` | — |
| `DGR-COP-PLANES-PAGO` | `PlanPagoMicroservice` | — |
| *(+ otros registrales)* | *(varios)* | — |

### Tópicos Internos / Snapshots (ReadSide consume)

| Tópico | Handler ReadSide | Tabla Cassandra |
|---|---|---|
| `ObjetoSnapshotPersistedReadside` | `ObjetoSnapshotPersistedHandler` | `read_side.buc_sujeto_objeto` |
| `SujetoSnapshotPersisted` | `SujetoSnapshotPersistedHandler` | `read_side.buc_sujeto` |
| `ObligacionPersistedSnapshot` | `ObligacionPersistedSnapshotHandler` | `read_side.buc_obligaciones` |

---

*Análisis generado automáticamente mediante inspección del código fuente del repositorio Copernico2.*
