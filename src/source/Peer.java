package source;

import storage.Backup;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer {

    private static final int NUMBER_ALLOWED_OF_ARGUMENTS = 7;

    public static void main(String args[]) {
        if (args.length != NUMBER_ALLOWED_OF_ARGUMENTS) {
            System.out.println("Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
            return;
        }

        //para já só cria diretórios
        //não faz mais nada
        //método de teste
        Backup.getInstance();
    }
}
