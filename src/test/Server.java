package test;

import java.io.*;
import java.net.*;

public class Server {

    private static int port;

    public static void main(String[] args) throws IOException {
        if (!validateArgs(args))
            return;

        ServerSocket srvSocket = null;
        Socket socket = null;

        boolean end = false;
        while(!end){
            try{
                srvSocket = new ServerSocket(port);
            } catch (IOException e){
                System.out.println("Could not listen on port: " + port);
                System.exit(-1);
            }

            try {
                socket = srvSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed: " + port);
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
            System.out.println(response);

            out.close();
            in.close();

            socket.close();
        }
        srvSocket.close();

    }

    private static boolean validateArgs(String[] args){
        if (args.length != 1){
            System.out.println("Usage: java Server <port>");
            return false;
        }
        else {
            port = Integer.parseInt(args[0]);
            System.out.println("Port: " + port);
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

}
