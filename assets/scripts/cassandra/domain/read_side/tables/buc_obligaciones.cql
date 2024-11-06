CREATE TABLE read_side.buc_obligaciones (
    bob_soj_identificador text,
    bob_soj_tipo_objeto text,
    bob_obn_id text,
    bob_suj_identificador text,
    bob_adherido_debito text,
    bob_canal_origen text,
    bob_capital float,
    bob_concepto text,
    bob_cuota text,
    bob_estado text,
    bob_exenta boolean,
    bob_fechasancion text,
    bob_fiscalizada text,
    bob_impuesto text,
    bob_indice_int_punit text,
    bob_indice_int_resar text,
    bob_interes_punit float,
    bob_interes_resar float,
    bob_jui_id int,
    bob_oga_id text,
    bob_otros_atributos map<text, text>,
    bob_periodo text,
    bob_pln_id text,
    bob_porcentaje_exencion float,
    bob_prorroga date,
    bob_resultdmn text,
    bob_saldo float,
    bob_soj_identificador_2 text,
    bob_sub_estado text,
    bob_supresiones map<text, text>,
    bob_tiene30obligacion boolean,
    bob_tipo text,
    bob_total float,
    bob_tpbid text,
    bob_vencimiento date,
    bob_vencimiento_2 date,
    PRIMARY KEY ((bob_soj_tipo_objeto, bob_soj_identificador), bob_suj_identificador, bob_obn_id)
) WITH CLUSTERING ORDER BY (bob_suj_identificador DESC, bob_obn_id DESC)
    AND bloom_filter_fp_chance = 0.01
    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
    AND comment = ''
    AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
    AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
    AND crc_check_chance = 1.0
    AND dclocal_read_repair_chance = 0.1
    AND default_time_to_live = 0
    AND gc_grace_seconds = 100
    AND max_index_interval = 2048
    AND memtable_flush_period_in_ms = 0
    AND min_index_interval = 128
    AND read_repair_chance = 0.0
    AND speculative_retry = '99PERCENTILE';