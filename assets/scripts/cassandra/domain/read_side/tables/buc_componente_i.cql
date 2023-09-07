CREATE TABLE IF NOT EXISTS read_side.buc_componente_i (
       bci_suj_identificador text,
       bci_soj_tipo_objeto text,
       bci_soj_identificador text,
       bci_obn_identificador text,
       bci_otros_atributos map<text, text>,
       bci_canal_origen text,
       bci_soj_identificador_2 text,
       PRIMARY KEY (bci_suj_identificador, bci_soj_tipo_objeto, bci_soj_identificador, bci_obn_identificador)
      ) WITH CLUSTERING ORDER BY (bci_soj_tipo_objeto ASC, bci_soj_identificador ASC, bci_obn_identificador ASC)
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


