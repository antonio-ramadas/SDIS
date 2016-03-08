package source;

import communication.Server;
import communication.Sockets;
import storage.Backup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer {

    public static void main(String args[]) {
        if (args.length != Arguments.NUMBER_ALLOWED_OF_ARGUMENTS.ordinal()) {
            System.out.println("Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
            return;
        }


        /*Path path = Paths.get("chunks/caminho.txt");
        try {
            byte[] data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Server.getInstance().setId(args[Arguments.SERVER_ID.ordinal()]);
        Server.getInstance().createChannel(Sockets.MULTICAST_CHANNEL,
                args[Arguments.MC_IP.ordinal()], args[Arguments.MC_PORT.ordinal()]);
        Server.getInstance().createChannel(Sockets.MULTICAST_DATA_CHANNEL,
                args[Arguments.MDB_IP.ordinal()], args[Arguments.MDB_PORT.ordinal()]);
        Server.getInstance().createChannel(Sockets.MULTICAST_DATA_RECOVERY,
                args[Arguments.MDR_IP.ordinal()], args[Arguments.MDR_PORT.ordinal()]);

        Server.getInstance().start();

        //para já só cria diretórios
        //não faz mais nada
        //método de teste
        Backup.getInstance();
    }
}
