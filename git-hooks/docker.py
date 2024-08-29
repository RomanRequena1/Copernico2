import pexpect

def cassandra():
    try:
        child = pexpect.spawn('docker exec -it prod_cassandra_1 cqlsh')
        child.expect(['cqlsh>'])
        child.sendline ('DESC tables;')
        child.expect ([
            "Keyspace read_side",
            "------------------",
            "buc_sujeto  buc_obligaciones  buc_sujeto_objeto"
        ])
        child.sendline("select * from read_side.buc_sujeto;")
        child.expect ([
            '-244.72'
        ])
        return True
    except:
        return False
