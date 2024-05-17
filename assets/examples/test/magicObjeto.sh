 
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
{"EV_ID": "$y$m$d$h$mi$s$n$resto","SOJ_SUJ_IDENTIFICADOR":"23-667147641-$we","SOJ_TIPO_OBJETO":"A","SOJ_IDENTIFICADOR":"ttt123456","SOJ_CAT_SOJ_ID":"TRI","SOJ_DESCRIPCION":"Des","SOJ_ESTADO":"TRANSF","SOJ_ID_EXTERNO":"436786342579","SOJ_CANAL_ORIGEN":"PSRM","SOJ_SUBTIPO":"AUTOMOTOR - Dueño Tipo de Vehículo 10","SOJ_TITULARIDAD":"CONDOMINIO","SOJ_OTROS_ATRIBUTOS":{"SOJ_DETALLES":[{"RESPONSABLE_OTROS_ATRIBUTOS":"S","PORCENTAJE_OTROS_ATRIBUTOS":"100","SOJ_OWNER":"Kripke, Barry"}]}}

EOF

done
