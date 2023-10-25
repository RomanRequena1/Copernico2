 #!/bin/bash
​
for we in `seq $1 $2`
do
    y=`date '+%Y'`
    m=`date '+%m'`
    d=`date '+%d'`
    h=`date '+%H'`
    #h="15"
    mi=`date '+%M'`
    #mi="04"
    s=`date '+%S'`
    n=`date '+%3N'`
    resto="000001084718625"
    #echo "$2 $we $2 $1"
    #`expr $2 - $we * 100 / ($2 - $1) expr: syntax error: unexpected argument «CalculateLag.txt»
    echo "JuicioObn $we"
    #sleep $3
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-JUICIOS-OBLIGACIONES-TRI" \
                -P <<EOF
{"EV_ID" : "$y$m$d$h$mi$s$n$resto","BJU_IDENTIFICADOR" : "JUICIO:503173552023","TEST": "TESTEANDING","BJD_SOJ_TIPO_OBJETO" : "I","BJD_SOJ_IDENTIFICADOR" : "280319008643","BJD_OBN_ID" : "20220000000019634312","BJD_BOB_PERIODO" : "2022","BJD_BOB_CUOTA" : "4","BJD_BOB_IMPUESTO" : "5","BJD_BOB_CONCEPTO" : "101","BJD_CANAL_ORIGEN" : "OTAX","BJD_BOB_SALDO" : "872.39","BJD_BOB_ESTADO" : "JUDICIAL","BJD_BOB_CAPITAL" : "648.29","BJD_BOB_VENCIMIENTO" : "2022-05-10 00:00:00.0","BJD_BOB_PRORROGA" : "2022-05-10 00:00:00.0","BJD_BOB_TIPO" : "tributaria","BJD_BOB_OGA_ID" : "12201","BJD_SOJ_ID_EXTERNO" : "1156465","BJD_BOB_JUI_ID" : "4317788","BJD_BOB_SUJ_IDENTIFICADOR" : "20-03242770-8","BJD_OTROS_ATRIBUTOS" : {  "BJD_DETALLES" : [ {    "RULE_NUMBER" : "1"  } ]}}

EOF

done

