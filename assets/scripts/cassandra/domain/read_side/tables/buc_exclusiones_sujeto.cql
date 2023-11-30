CREATE TABLE IF NOT EXISTS read_side.buc_sujeto_exclusiones (
    bse_suj_identificador text,
    bse_accion text,
    bse_motivo text,
    bse_fecha_desde date,
    bse_fecha_hasta date,
    bse_origen text,
    bse_tramite_id text,
    bse_nombre_gestion text,
    bse_numero_gestion text,
    bse_tipo_exclusion text,
    PRIMARY KEY (bse_suj_identificador)
) WITH bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'sstable_compression': 'LZ4Compressor', 'chunk_length_in_kb': '64'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 100
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';
