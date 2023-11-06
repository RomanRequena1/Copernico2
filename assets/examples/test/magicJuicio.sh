 

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
    echo "Juicio $we"
    #sleep $3
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-JUICIOS-TRI" \
                -P <<EOF
{"EV_ID" : "$y$m$d$h$mi$s$n$resto","BJU_SUJ_IDENTIFICADOR" : "111122324075315","BJU_SOJ_TIPO_OBJETO" : "A","BJU_SOJ_IDENTIFICADOR" : "22110","BJU_JUI_ID" : "$we","BJU_CAPITAL" : "1073.93","BJU_ESTADO" : null,"BJU_FISCALIZADA" : null,"BJU_GASTOS" : "9.4","BJU_GASTOS_MART" : null,"BJU_HONORARIOS" : "619.81","BJU_HONORARIOS_MART" : null,"BJU_INICIO_DEMANDA" : "2014-11-25 00:00:00.0","BJU_INTERES_PUNIT" : null,"BJU_INTERES_RESAR" : null,"BJU_PCR_ID" : "55467","BJU_PORCENTAJE_IVA" : "21","BJU_PROCURADOR" : "FERREYRA SOLEDAD DEL CARMEN","BJU_TIPO" : "APRE","BJU_TOTAL" : "2470.91","BJU_NRO_EXTERNO" : "test","FECHA_BAJA" : null,"BJU_OTROS_ATRIBUTOS" : {  "BJU_DETALLES" : [ {    "BJU_OBLIGACION" : "19940000000007261586",    "BJU_IMPUESTO" : "600",    "BJU_CONCEPTO" : "5",    "BJU_PERIODO" : "5",    "BJU_CUOTA" : "5",    "BJU_FECHA_GENERACION" : "2014-10-29 00:00:00.0",    "BJU_FECHA_IMPRESION" : "2014-11-25 00:00:00.0",    "BJU_VTO_ORIGINAL" : null,    "BJU_IMPORTE_ORIGINAL" : null,    "BJU_IMPORTE_HISTORICO" : null,    "BJU_INTERES" : null,    "BJU_INTERES_LIQUIDACION" : null,    "BJU_ID_MARTILLERO_OTROS_ATRIBUTOS" : null,    "BJU_MARTILLERO_OTROS_ATRIBUTOS" : null  } ]}}





EOF

done
