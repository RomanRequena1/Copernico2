#!/bin/bash

BATCH_FILE="/tmp/kafka_events_batch.json"
> $BATCH_FILE  # Limpiar archivo

echo "Generando eventos..."
for we in $(seq $1 $2); do
    y=$(date '+%Y')
    m=$(date '+%m')
    d=$(date '+%d')
    h=$(date '+%H')
    mi=$(date '+%M')
    s=$(date '+%S')
    n=$(date '+%3N')
    resto="000001084718625"

    echo "{\"EV_ID\":\"$y$m$d$h$mi$s$n$resto\",\"BOB_SUJ_IDENTIFICADOR\":\"20-03034769-$we\",\"BOB_SOJ_TIPO_OBJETO\":\"A\",\"BOB_SOJ_IDENTIFICADOR\":\"AUTO09102$we\",\"BOB_OBN_ID\":\"20250000000005413$we\",\"BOB_SALDO\":\"31367.69\",\"BOB_CUOTA\":\"7\",\"BOB_ESTADO\":\"ADMINISTRATIVA\",\"BOB_CANAL_ORIGEN\":\"OTAX\",\"BOB_FISCALIZADA\":\"N\",\"BOB_JUI_ID\":null,\"BOB_PERIODO\":\"2025\",\"BOB_PLN_ID\":null,\"BOB_PRORROGA\":\"2025-08-10 00:00:00.0\",\"BOB_TIPO\":\"tributaria\",\"BOB_VENCIMIENTO\":\"2025-08-10 00:00:00.0\",\"BOB_CAPITAL\":\"31367.69\",\"BOB_CONCEPTO\":\"601\",\"BOB_IMPUESTO\":\"600\",\"BOB_ADHERIDO_DEBITO\":\"N\",\"BOB_OGA_ID\":\"62501\",\"BOB_VENCIMIENTO_2\":null,\"SOJ_ID_EXTERNO\":\"7951142$we\",\"BOB_SOJ_IDENTIFICADOR_2\":null,\"BOB_OTROS_ATRIBUTOS\":{\"BOB_DETALLES\":[{\"RULE_NUMBER\":\"1\"}]},\"BOB_SUPRESIONES\":null}" >> $BATCH_FILE
done

echo "Enviando $(($2 - $1 + 1)) eventos en batch a Kafka..."
cat $BATCH_FILE | kafkacat -b 0.0.0.0:9092 -t "DGR-COP-OBLIGACIONES-TRI-A-SINCRO" -P

rm $BATCH_FILE
echo "✓ Eventos enviados exitosamente"
