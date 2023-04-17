 
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
    echo "Obligacion $we"
    #sleep $3
    kafkacat -b 0.0.0.0:9092 \
                -t "DGR-COP-OBLIGACIONES-TRI-E" \
                -P <<EOF
{"time":"$y-$m-$d $h:$mi:$s.$n","ev_id": "$y$m$d$h$mi$s$n$resto","paso_0": "E","time_paso_0": "$y-$m-$d $h:$mi:$s.$n"}
EOF
    
done
