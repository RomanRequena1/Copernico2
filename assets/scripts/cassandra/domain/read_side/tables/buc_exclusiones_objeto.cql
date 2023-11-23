CREATE TABLE IF NOT EXISTS read_side.buc_exclusiones_objeto (
    boe_obj_id text,
    boe_accion text,
    boe_motivo text,
    boe_fecha_desde date,
    boe_fecha_hasta date,
    boe_origen text,
    boe_tramite_id text,
    boe_nombre_gestion text,
    boe_numero_gestion text,
    boe_tipo_exclusion text,
    PRIMARY KEY (boe_obj_id)
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
