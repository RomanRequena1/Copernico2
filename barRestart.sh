#!/bin/bash

echo "Iniciando reinicio..."
# Total steps in the script
TOTAL_STEPS=22
CURRENT_STEP=0
# Function to update progress
update_progress() {
  CURRENT_STEP=$((CURRENT_STEP + 1))
  PERCENT=$((CURRENT_STEP * 100 / TOTAL_STEPS))
  echo -ne "Progreso: ["
  for ((i = 0; i < PERCENT / 2; i++)); do echo -ne "\033[1;32m█\033[0m"; done
  for ((i = PERCENT / 2; i < 50; i++)); do echo -n " "; done
  echo -ne "] $PERCENT% completado $PASO \r"
}

# Script tasks
PASO="(Eliminando tópicos de Kafka...)"
update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic DGR-COP-SUJETO-TRI > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic DGR-COP-OBJETOS-TRI > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic DGR-COP-OBLIGACIONES-TRI > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic SujetoSnapshotPersisted > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic ObjetoSnapshotPersistedReadside > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --delete --bootstrap-server localhost:9092 --topic ObligacionPersistedSnapshot > /dev/null 2>&1;

PASO="(Truncando tablas de Cassandra...)"
update_progress
docker exec -i cassandra cqlsh -e "truncate table akka.all_persistence_ids;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table akka.messages;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table akka.metadata;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table akka_snapshot.snapshots;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table akka.tag_scanning;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table akka.tag_views;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table akka.tag_write_progress;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table read_side.buc_sujeto;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table read_side.buc_sujeto_objeto;" > /dev/null 2>&1;

update_progress
docker exec -i cassandra cqlsh -e "truncate table read_side.buc_obligaciones;" > /dev/null 2>&1;

PASO="(Creando tópicos de Kafka...)        "
update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic DGR-COP-SUJETO-TRI > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic DGR-COP-OBJETOS-TRI > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic DGR-COP-OBLIGACIONES-TRI > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic SujetoSnapshotPersisted > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic ObjetoSnapshotPersistedReadside > /dev/null 2>&1;

update_progress
docker exec -i kafka /opt/kafka/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic ObligacionPersistedSnapshot > /dev/null 2>&1;

# Finish
echo -ne "\nProceso completado.\n"
