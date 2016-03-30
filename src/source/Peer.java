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

import java.io.*;
import java.net.*;
import console.MessageCenter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Peer {

    private static int port;

    public static void main(String args[]) throws IOException {

        if (!validateArgs(args))
            return;

        ServerSocket srvSocket = null;
        Socket socket = null;

        try{
            srvSocket = new ServerSocket(port);
        } catch (IOException e){
            MessageCenter.output("Could not listen on port: " + port);
            System.exit(-1);
        }

        boolean end = false;
        while(!end){

            try {
                socket = srvSocket.accept();
            } catch (IOException e) {
                MessageCenter.error("Accept failed: " + port);
                System.exit(1);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String request = in.readLine();

            String[] splitRequest = request.split(" ");


            String sub_protocol = splitRequest[0];
            String response;

            if (sub_protocol.equals("BACKUP")){
                String opnd_1 = splitRequest[1];
                int opnd_2 = Integer.parseInt(splitRequest[2]);
                response = backup(opnd_1, opnd_2);
            }
            else if (sub_protocol.equals("RESTORE")){
                String opnd_1 = splitRequest[1];
                response = restore(opnd_1);
            }
            else if (sub_protocol.equals("DELETE")){
                String opnd_1 = splitRequest[1];
                response = delete(opnd_1);
            }
            else if (sub_protocol.equals("SPACE")){
                int opnd_1 = Integer.parseInt(splitRequest[1]);
                response = space(opnd_1);
            }
            else {
                response = "Without response";
            }

            out.println(response);
            MessageCenter.output(response);

            out.close();
            in.close();

            socket.close();
        }
        srvSocket.close();
    }

    private static boolean validateArgs(String[] args){
        if (args.length != 1){
            MessageCenter.output("Usage: java Server <port>");
            return false;
        }
        else {
            port = Integer.parseInt(args[0]);
            MessageCenter.output("Port: " + port);
            return true;
        }
    }

    private static String backup(String filePath, int replicationDegree){
        return "backup " + filePath + " " + replicationDegree;
    }

    private static String delete(String filePath){
        return "delete " + filePath;
    }

    private static String restore(String filePath){
        return "restore " + filePath;
    }

    private static String space(int amountOfSpace){
        return "space " + amountOfSpace;
    }

    // Corrigir
    public static void splitFile(File f) throws IOException {
        int partCounter = 1;

        int sizeOfFiles = 64 * 1024;// 64 Kb
        byte[] buffer = new byte[sizeOfFiles];

        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f))) {//try-with-resources to ensure closing stream
            String name = f.getName();

            int tmp = 0;
            while ((tmp = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                File newFile = new File(f.getParent(), name + "."
                        + String.format("%03d", partCounter++));
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, tmp);//tmp is chunk size
                }
            }
        }
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

        Server.getInstance().start();

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
