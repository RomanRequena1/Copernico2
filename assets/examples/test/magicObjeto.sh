 
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
    echo "Objeto $we"
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-OBJETOS-TRI" \
                -P <<EOF
{"EV_ID": "$y$m$d$h$mi$s$n$resto","SOJ_SUJ_IDENTIFICADOR": "20-21392777-5","SOJ_TIPO_OBJETO": "M","NUEVOCAMPO": "NUEVOCAMPO","SOJ_IDENTIFICADOR": "PP:2023105253777","SOJ_CAT_SOJ_ID": "TRI","SOJ_DESCRIPCION": "PLAN DE PAGOS","SOJ_ESTADO": "rfthrthrt","SOJ_FECHA_FIN": null,"SOJ_FECHA_INICIO": "2023-07-04 08:25:31.0","SOJ_ID_EXTERNO": "253402918","SOJ_BASE_IMPONIBLE": null,"SOJ_ADHERIDO_DEBITO": "S","SOJ_TITULARIDAD": null,"SOJ_CANAL_ORIGEN": "OTAX"}
EOF

done
