CREATE TABLE IF NOT EXISTS readside.buc_objeto_rel (
rjp_soj_identificador   text,
rjp_soj_tipo_objeto     text,
rjp_identificador_rel   text,
rjp_tipo_objeto_rel     text,
rjp_tipo_rel            text,
rjp_id_externo          text,
rjp_id_externo_2        text,
rjp_estado              text,
PRIMARY KEY (rjp_soj_identificador, rjp_soj_tipo_objeto, rjp_identificador_rel, rjp_tipo_objeto_rel))
WITH CLUSTERING ORDER BY (rjp_soj_identificador ASC, rjp_soj_tipo_objeto ASC, rjp_identificador ASC rjp_tipo_objeto ASC)
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