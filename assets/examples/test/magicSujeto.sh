 

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
    echo "Sujeto $we"
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-SUJETO-TRI" \
                -P <<EOF
{"EV_ID":"$y$m$d$h$mi$s$n$resto","SUJ_IDENTIFICADOR": "20-06411831-7","RIVERPLATE": "RIVERCAMPEON","SUJ_CAT_SUJ_ID": "3","TESTSUJ": "TESTEANDO", "SUJ_DENOMINACION": "UNIVERSO BELLEZA S R L","SUJ_DFE": null,"SUJ_DIRECCION": null,"SUJ_EMAIL": "RIVERPLATE@HOTMAIL.COM","SUJ_ID_EXTERNO": "7834365","SUJ_OTROS_ATRIBUTOS": null,"SUJ_RIESGO_FISCAL": null,"SUJ_SITUACION_FISCAL": null,"SUJ_TELEFONO": "03472-482924","SUJ_TIPO": "A","FECHA_BAJA": "2006-07-21 11:41:25.0","SUJ_CANAL_ORIGEN": "a"}
EOF

done
