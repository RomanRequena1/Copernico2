CREATE TABLE IF NOT EXISTS read_side.buc_plan_pago_detalles  (
    bpl_identificador text,
    bpd_soj_tipo_objeto text,
    bpd_soj_identificador text,
    bpd_obn_id text,
    bpd_bob_periodo date,
    bpd_bob_cuota text,
    bpd_bob_impuesto text,
    bpd_bob_concepto text,
    bpd_canal_origen text,
    rule_number text,
    bpd_estado text,
    bpd_bob_importe_a_financiar float,
    bpd_bob_vencimiento date,
    bpd_bob_prorroga date,
    bpd_bob_tipo text,
    bpd_bob_oga_id text,
    bpd_bju_identificador text,
    PRIMARY KEY (bpl_identificador, bpd_soj_tipo_objeto, bpd_soj_identificador, bpd_obn_id)
) WITH CLUSTERING ORDER BY (bpd_soj_tipo_objeto ASC,bpd_soj_identificador ASC,bpd_obn_id ASC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'sstable_compression': 'LZ4Compressor', 'chunk_length_in_kb': '64'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 864000
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';

