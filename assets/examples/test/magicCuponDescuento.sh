
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
    echo "CuponDescuento $we"
    #sleep $3
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-CUPON-DESCUENTO-TRI" \
                -P <<EOF
{"EV_ID": "$y$m$d$h$mi$s$n$resto","BOB_SUJ_IDENTIFICADOR": "91-99999999-8","TEST": "TESTEANDDODODO","BOB_OBN_ID": "30997131966994","BOB_SOJ_IDENTIFICADOR": "JJVB1977_4","BOB_SOJ_TIPO_OBJETO": "I","BOB_CANAL_ORIGEN": "PSRM","bdc_prorroga": "2023-10-10","BOB_OTROS_ATRIBUTOS": {"BOB_DETALLES": [  {    "bdc_tipo": "DESC1",    "bdc_descripcion": "Descuento 30% Provincial",    "bdc_monto": "13793.07",    "bdc_vencimiento": "2023-10-10 00:00:00.0"  },  {    "bdc_tipo": "DESC4",    "bdc_descripcion": "Descuento 30% Municipal",    "bdc_monto": "13793.07",    "bdc_vencimiento": "2023-10-10 00:00:00.0"  }]}}
EOF

done
