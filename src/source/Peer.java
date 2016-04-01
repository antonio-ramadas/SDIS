package source;

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
import test.TestApp;
import threads.*;


import java.io.*;
import java.net.*;
import console.MessageCenter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer {

    private static int server_port;
    private static String server_id;
    private static String MC_IP;
    private static String MC_PORT;
    private static String MDB_IP;
    private static String MDB_PORT;
    private static String MDR_IP;
    private static String MDR_PORT;


    public static void main(String args[]) throws IOException {

        if (!validateArgs(args))
            return;

        ServerSocket srvSocket = null;
        Socket socket = null;

        try{
            srvSocket = new ServerSocket(server_port);
        } catch (IOException e){
            MessageCenter.output("Could not listen on port: " + server_port);
        }

        Server.getInstance().setId(server_id);
        Server.getInstance().createChannel(Sockets.MULTICAST_CHANNEL, MC_IP, MC_PORT);
        Server.getInstance().createChannel(Sockets.MULTICAST_DATA_CHANNEL, MDB_IP, MDB_PORT);
        Server.getInstance().createChannel(Sockets.MULTICAST_DATA_RECOVERY, MDR_IP, MDR_PORT);
        Server.getInstance().start();



        boolean end = false;
        while(!end){

            try {
                socket = srvSocket.accept();
            } catch (IOException e) {
                MessageCenter.error("Accept failed: " + server_port);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String request = in.readLine();

            String[] splitRequest = request.split(" ");


            String sub_protocol = splitRequest[0];

            if (sub_protocol.equals("BACKUP")){
                String opnd_1 = splitRequest[1];
                int opnd_2 = Integer.parseInt(splitRequest[2]);
                backup(opnd_1, opnd_2);
            }
            else if (sub_protocol.equals("RESTORE")){
                String opnd_1 = splitRequest[1];
                restore(opnd_1);
            }
            else if (sub_protocol.equals("DELETE")){
                String opnd_1 = splitRequest[1];
                delete(opnd_1);
            }
            else if (sub_protocol.equals("SPACE")){
                int opnd_1 = Integer.parseInt(splitRequest[1]);
                space(opnd_1);
            }
            else {
                out.println("Error reading subprotocol");
            }

            out.close();
            in.close();

            socket.close();
        }
        srvSocket.close();
    }

    private static boolean validateArgs(String[] args){
        if (args.length != Arguments.NUMBER_ALLOWED_OF_ARGUMENTS.ordinal()){
            MessageCenter.output("Usage: Peer <server_id> <MC_IP> <MC_PORT> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_PORT>");
            return false;
        }
        else {
            server_id = args[Arguments.SERVER_ID.ordinal()];
            server_port = Integer.parseInt(args[Arguments.SERVER_ID.ordinal()]);
            server_port += 8000;
            MC_IP = args[Arguments.MC_IP.ordinal()];
            MC_PORT = args[Arguments.MC_PORT.ordinal()];
            MDB_IP = args[Arguments.MDB_IP.ordinal()];
            MDB_PORT = args[Arguments.MDB_PORT.ordinal()];
            MDR_IP = args[Arguments.MDR_IP.ordinal()];
            MDR_PORT = args[Arguments.MDR_PORT.ordinal()];
            MessageCenter.output("Peer " + server_id + " " + MC_IP + " " + MC_PORT + " " + MDB_IP + " " + MDB_PORT + " " + MDR_IP + " " + MDR_PORT);
            return true;
        }
    }

    private static void backup(String filePath, int replicationDegree) throws RemoteException {
        File file = new File(filePath);
        new Thread(new backupThread(file, replicationDegree)).start();
    }

    /*
        Message m = new Message("PUTCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 \r\n\r\njhg\n".getBytes());
        m.decompose();
        m.compose();
        MessageCenter.output(m.toString());
        m = new Message("PUTCHUNK 1.0 antonio 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 \r\n\r\nasdasd".getBytes());
        m.decompose();
        m.compose();
        MessageCenter.output(m.toString());
        Server.getInstance().send(MessageTypes.CHUNK, "PUTCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 1 \r\n\r\nsasaas".getBytes());

        ChunkBackup CB = new ChunkBackup(m);
        CB.send();
    */

    private static void delete(String filePath) throws RemoteException{
        File file = new File(filePath);
        new Thread(new deleteThread(file)).start();
    }

    private static void restore(String filePath) throws RemoteException{
        File file = new File(filePath);
        new Thread(new restoreThread(file)).start();
    }
    /*
        Message m = new Message("GETCHUNK 1.0 teste 7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069 55555 \r\n\r\n".getBytes());
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
    */

    private static void space(int amountOfSpace){
        new Thread(new spaceThread(amountOfSpace)).start();
    }



    // Corrigir esta parte
    public static void mergeFiles(List<File> files, File into)
            throws IOException {
        try (BufferedOutputStream mergingStream = new BufferedOutputStream(
                new FileOutputStream(into))) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
            }
        }
    }

        /*

        //from here below there are only tests
        //this is for debug ONLY
        System.err.println("--------------");
        System.err.println("Debug section!");
        System.err.println("--------------");



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

//        Path path = Paths.get("files/chunks/caminho.txt");
//        try {
//            byte[] data = Files.readAllBytes(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //para já só cria diretórios
        //não faz mais nada
        //método de teste
        Backup.getInstance();
    }*/
}
