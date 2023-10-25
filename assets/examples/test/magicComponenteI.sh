 
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
    echo "ComponenteI $we"
    #sleep $3
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-COMPONENTE-I-TRI" \
                -P <<EOF
{"EV_ID" : "$y$m$d$h$mi$s$n$resto","BOB_SUJ_IDENTIFICADOR" : "20-14921826-3","TEST": "TESTEAANDOOOOO","BOB_OBN_ID" : "4644886183","BOB_SOJ_IDENTIFICADOR" : "9115","BOB_SOJ_TIPO_OBJETO" : "N","BOB_CANAL_ORIGEN" : "PSRM","BOB_OTROS_ATRIBUTOS" : {"BOB_DETALLES" : [ {"sequence" : "1","ruleDescription" : "Crédito por Cota Mensual - Provincial","amountCalculated" : "-9647.4600000","distributionId" : "EMBACOTA"}, {"sequence" : "2","ruleDescription" : "Impuesto Básico - Provincial Mensual","amountCalculated" : "10261.5900000","distributionId" : "EMBASMT"} ]}}
EOF

done
