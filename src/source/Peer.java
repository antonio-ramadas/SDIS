package source;

import cli.CLI_Arguments;
import cli.Protocols;
import cli.RMI_Message;
import cli.TestApp;
import communication.Request;
import communication.Server;
import communication.Sockets;
import console.MessageCenter;
import message.Message;
import message.MessageTypes;
import protocols.ChunkBackup;
import protocols.ChunkRestore;
import storage.Backup;
import storage.Chunk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer implements RMI_Message {

    @Override
    public void handlerRMI(Protocols protocol, String[] args) throws RemoteException {
        switch (protocol) {
            case BACKUP:
                Server.getInstance().backUp(args);
                break;
            case RESTORE:
                Server.getInstance().restore(args, false);
                break;
            case RESTOREENH:
                Server.getInstance().restore(args, true);
                break;
            case DELETE:
                Server.getInstance().delete(args);
                break;
            case RECLAIM:
                Server.getInstance().reclaim(args);
                break;
        }
    }

    private static final int DEFAULT_RMI_PORT = 1099;

    public static void main(String args[]) {
        if (args.length != Arguments.NUMBER_ALLOWED_OF_ARGUMENTS.ordinal()) {
            System.out.println("Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
            return;
        }

        Server.getInstance().setId(args[Arguments.SERVER_ID.ordinal()]);
        Server.getInstance().createChannel(Sockets.MULTICAST_CHANNEL,
                args[Arguments.MC_IP.ordinal()], args[Arguments.MC_PORT.ordinal()]);
        Server.getInstance().createChannel(Sockets.MULTICAST_DATA_CHANNEL,
                args[Arguments.MDB_IP.ordinal()], args[Arguments.MDB_PORT.ordinal()]);
        Server.getInstance().createChannel(Sockets.MULTICAST_DATA_RECOVERY,
                args[Arguments.MDR_IP.ordinal()], args[Arguments.MDR_PORT.ordinal()]);
        Server.getInstance().startRestoreEnhancement();

        Server.getInstance().start();

        try {
            LocateRegistry.createRegistry(DEFAULT_RMI_PORT);
        } catch (RemoteException e) {
            MessageCenter.output("Registry already exists");
        }

        try {
            Peer obj = new Peer();
            RMI_Message stub = (RMI_Message) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(DEFAULT_RMI_PORT);
            registry.bind(Server.getInstance().getId(), stub);

            MessageCenter.output("Peer ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }

        /*
        //from here below there are only tests
        //this is for debug ONLY
        System.err.println("--------------");
        System.err.println("Debug section!");
        System.err.println("--------------");
        Message m = new Message("PUTCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 \r\n\r\njhg\n".getBytes());
        m.decompose();
        m.compose();
        System.out.println(m);
        m = new Message("PUTCHUNK 1.0 antonio 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 \r\n\r\nasdasd".getBytes());
        m.decompose();
        m.compose();
        System.out.println(m);
        Server.getInstance().send(MessageTypes.CHUNK, "PUTCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 \r\n\r\nsasaas".getBytes());

        ChunkBackup CB = new ChunkBackup(m);
        CB.send();

        m = new Message("GETCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 \r\n\r\n".getBytes());
        m.decompose();
        ChunkRestore CR = new ChunkRestore(m);
        CR.send();

        m = new Message("GETCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200121daad9 55555 \r\n\r\n".getBytes());
        m.decompose();
        CR = new ChunkRestore(m);
        CR.send();

        m = new Message("GETCHUNK 1.9 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 \r\n\r\n".getBytes());
        m.decompose();
        CR = new ChunkRestore(m);
        CR.send();

        m = new Message("GETCHUNK 1.9 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200122daad9 55555 \r\n\r\n".getBytes());
        m.decompose();
        CR = new ChunkRestore(m);
        CR.send();

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            String text = "This is some text";

            md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();

            char[] hexArray = "0123456789ABCDEF".toCharArray();
            char[] hexChars = new char[digest.length * 2];
            for ( int j = 0; j < digest.length; j++ ) {
                int v = digest[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }

            System.out.println(new String(hexChars));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.print(true);

//        Path path = Paths.get("files/chunks/caminho.txt");
//        try {
//            byte[] data = Files.readAllBytes(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //para já só cria diretórios
        //não faz mais nada
        //método de teste
        Backup.getInstance();*/
    }

}
