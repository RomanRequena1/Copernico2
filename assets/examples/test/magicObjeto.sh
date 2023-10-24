 
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
{"EV_ID":"$y$m$d$h$mi$s$n$resto","SOJ_SUJ_IDENTIFICADOR": "20-06411831-7","TEST": "TESSSSSSSSST","SOJ_TIPO_OBJETO": "E","TEST2": "testeando","TESTTTTT": "ALOKASE","SOJ_IDENTIFICADOR": "210246428","SOJ_CAT_SOJ_ID": "TRI","SOJ_DESCRIPCION": "PLAN DE PAGOS","SOJ_ESTADO": "ESTADING","SOJ_FECHA_FIN": null,"SOJ_FECHA_INICIO": "2023-07-04 08:25:31.0","SOJ_ID_EXTERNO": "253402918","SOJ_BASE_IMPONIBLE": null,"SOJ_ADHERIDO_DEBITO": "S","SOJ_TITULARIDAD": null,"SOJ_CANAL_ORIGEN": "OTAX","SOJ_OTROS_ATRIBUTOS": {"SOJ_DETALLES": [{"RESPONSABLE_OTROS_ATRIBUTOS": null,"PORCENTAJE_OTROS_ATRIBUTOS": null,"OTROS_ATRIBUTOS_ADHERIDO_DEBITO": null,"CUENTA_SOJ_OTROS_ATRIBUTOS": null,"PERIODO_SOJ_OTROS_ATRIBUTOS": null,"IMPORTE_SOJ_OTROS_ATRIBUTOS": null,"FECHA_BAJA": null}]}}
EOF

done
