
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
    echo "JuicoTri $we"
    #sleep $3
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-JUICIOS-CAB-TRI" \
                -P <<EOF
{"EV_ID" : "$y$m$d$h$mi$s$n$resto","BJU_IDENTIFICADOR" : "JUICIO:1400917197","TEST": "TESTEANDINGGGG","BJU_JUI_ID" : "52502","BJU_NRO_EXTERNO" : "1400917197","BJU_CAPITAL" : "566.4","BJU_TOTAL" : "1342.49","BJU_ESTADO" : "tyjhtyjty","BJU_FECHA_GENERACION" : "1999-12-29 00:00:00.0","BJU_FECHA_IMPRESION" : null,"BJU_TIPO" : "S","BJU_TIPO_JUICIO" : "APRE","BJU_SUJ_IDENTIFICADOR" : "20-03072712-7","BJU_CUIT_ORIGEN" : "210306368651","BJU_SOJ_TIPO_OBJETO" : "I","BJU_SOJ_IDENTIFICADOR" : "210306368651","BJU_IPO_ID" : "5","BJU_CANAL_ORIGEN" : "OTAX"}




EOF

done
