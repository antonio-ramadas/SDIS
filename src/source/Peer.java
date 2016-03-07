package source;

import communication.Server;
import storage.Backup;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer {

    public static void main(String args[]) {
        if (args.length != Arguments.NUMBER_ALLOWED_OF_ARGUMENTS.ordinal()) {
            System.out.println("Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
            return;
        }

        Server.getInstance().setId(args[Arguments.SERVER_ID.ordinal()]);
        Server.getInstance().setMC(args[Arguments.MC_IP.ordinal()], args[Arguments.MC_PORT.ordinal()]);
        Server.getInstance().setMDB(args[Arguments.MDB_IP.ordinal()], args[Arguments.MDB_PORT.ordinal()]);
        Server.getInstance().setMDR(args[Arguments.MDR_IP.ordinal()], args[Arguments.MDR_PORT.ordinal()]);

        Server.getInstance().start();

        //para já só cria diretórios
        //não faz mais nada
        //método de teste
        Backup.getInstance();
    }
}
