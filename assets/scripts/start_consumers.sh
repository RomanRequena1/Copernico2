#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-CALENDARIO

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-DECJURADAS

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-DOMICILIO-OBJ-ANT
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-DOMICILIO-OBJ-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-DOMICILIO-SUJ-ANT
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-DOMICILIO-SUJ-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-ETAPROCESALES-ANT
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-ETAPROCESALES-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-JUICIOS-ANT

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-JUICIOS-TRI
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-JUICIOS-OBLIGACIONES-TRI
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-OBJETOS-ANT
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-OBJETOS-TRI

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-OBLIGACIONES-ANT
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-OBLIGACIONES-TRI

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-EXCLUSIONES-OBJETO-TRI
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-EXCLUSIONES-SUJETO-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PARAMPLAN-ANT
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PARAMPLAN-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PARAMRECARGO-ANT
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PARAMRECARGO-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PLANES-ANT
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PLANES-TRI

#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUBASTAS

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-ANT
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-SUJETO-TRI



#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-TRAMITES

curl -X POST http://0.0.0.0:8081/kafka/start/ObjetoSnapshotPersisted
curl -X POST http://0.0.0.0:8081/kafka/start/ObligacionPersistedSnapshot

curl -X POST http://0.0.0.0:8081/kafka/start/ObjetoReceiveSnapshot
curl -X POST http://0.0.0.0:8081/kafka/start/SujetoReceiveSnapshot

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-CUPON-DESCUENTO-TRI
curl -X POST http://0.0.0.0:8084/kafka/start/CuponDescuentoPersistedSnapshot

curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-COMPONENTE-I-TRI
curl -X POST http://0.0.0.0:8084/kafka/start/ComponenteIPersistedSnapshot

curl -X POST http://0.0.0.0:8084/kafka/start/JuicioObnDeletedFronDto
curl -X POST http://0.0.0.0:8084/kafka/start/JuicioObnUpdatedFronDto
curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-PLAN-CABECERA-TRI
#curl -X POST http://0.0.0.0:8081/kafka/start/DGR-COP-EXENCIONES

curl -X POST http://0.0.0.0:8081/kafka/start/ExclusionesObjetoUpdatedFromDto
curl -X POST http://0.0.0.0:8081/kafka/start/ExclusionesSujetoUpdatedFromDto
