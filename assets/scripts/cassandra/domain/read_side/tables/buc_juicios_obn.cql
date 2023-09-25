CREATE TABLE read_side.buc_juicios_obn (
       bju_identificador text,
       bjd_soj_tipo_objeto text,
       bjd_soj_identificador text,
       bjd_obn_id text,
       bjd_bob_periodo text,
       bjd_bob_cuota text,
       bjd_bob_impuesto text,
       bjd_bob_concepto text,
       bjd_canal_origen text,
       bjd_bob_saldo float,
       bjd_bob_estado text,
       bjd_bob_capital float,
       bjd_bob_vencimiento date,
       bjd_bob_prorroga date,
       bjd_bob_tipo text,
       bjd_bob_oga_id text,
       bjd_soj_id_externo text,
       bjd_bob_jui_id text,
       bjd_bob_suj_identificador text,
       bjd_otros_atributos map<text, text>,
       PRIMARY KEY (bju_identificador, bjd_soj_tipo_objeto, bjd_soj_identificador, bjd_obn_id)
      ) WITH CLUSTERING ORDER BY (bjd_soj_tipo_objeto ASC, bjd_soj_identificador ASC, bjd_obn_id ASC)
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