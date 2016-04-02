Compilar:
javac source/Peer.java
javac cli/TestApp.java

Executar:
java source.Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>
java cli.TestApp [conforme descrito no guião]

Exemplos:
java source.Peer 1000 224.0.0.2 14000 224.0.0.3 14001 224.0.0.4 14002
java source.Peer 2000 224.0.0.2 14000 224.0.0.3 14001 224.0.0.4 14002
java source.Peer 3000 224.0.0.2 14000 224.0.0.3 14001 224.0.0.4 14002
java source.Peer 4000 224.0.0.2 14000 224.0.0.3 14001 224.0.0.4 14002

java cli.TestApp 1000 BACKUP 1.jpg 2
java cli.TestApp 1000 DELETE 1.jpg
java cli.TestApp 1000 RECLAIM 20000
java cli.TestApp 1000 RESTORE 1.jpg
java cli.TestApp 1000 RESTOREENH 1.jpg