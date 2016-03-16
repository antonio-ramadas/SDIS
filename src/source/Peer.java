package source;

import communication.Server;
import communication.Sockets;
import message.Message;
import storage.Backup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer {

    public static void main(String args[]) {
        if (args.length != Arguments.NUMBER_ALLOWED_OF_ARGUMENTS.ordinal()) {
            System.out.println("Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
            return;
        }


        //from here below there are only tests
        //this is for debug ONLY
        System.err.println("--------------");
        System.err.println("Debug section!");
        System.err.println("--------------");
        Message m = new Message("PUTCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 iiiidata".getBytes());
        m.decompose();
        m.compose();


        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            String text = "This is some text";

            md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Path path = Paths.get("files/chunks/caminho.txt");
        try {
            byte[] data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
