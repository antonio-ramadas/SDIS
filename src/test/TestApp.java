package test;

import java.io.*;
import java.net.*;

public class TestApp {

    private static String address, peer_ap, sub_protocol, opnd_1, opnd_2;
    private static int port;

    public static void main(String[] args) throws IOException {

        if (!validateArgs(args)) {
            return;
        }

        String request = sub_protocol;
        if (sub_protocol.equals("BACKUP")){
            request += " " + opnd_1 + " " + opnd_2;
        }
        else {
            request += " " + opnd_1;
        }


        // send request
        Socket socket = new Socket(address, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println(request);
        String response = in.readLine();

        System.out.println(response);

        out.close();
        in.close();

        socket.close();
    }

    private static boolean validateArgs(String[] args) {
        if (args.length < 3 || args.length > 4){
            System.out.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return false;
        }
        else{
            peer_ap = args[0];
            String[] peer_ap_split = peer_ap.split(":");
            address = peer_ap_split[0];
            port = Integer.parseInt(peer_ap_split[1]);

            sub_protocol = args[2];

            if (sub_protocol.equals("BACKUP")){
                if (args.length != 4) {
                    System.out.println("Usage: java TestApp <address:port> BACKUP <file path> <replication degree>");
                    return false;
                }
                opnd_1 = args[2];
                opnd_2 = args[3];
            }
            else if (sub_protocol.equals("RESTORE")){
                if (args.length != 3) {
                    System.out.println("Usage: java TestApp <address:port> RESTORE <file path>");
                    return false;
                }
                opnd_1 = args[2];
            }
            else if (sub_protocol.equals("DELETE")){
                if (args.length != 3) {
                    System.out.println("Usage: java TestApp <address:port> DELETE <file path>");
                    return false;
                }
                opnd_1 = args[2];
            }
            else if (sub_protocol.equals("SPACE")){
                if (args.length != 3) {
                    System.out.println("Usage: java TestApp <address:port> SPACE <amount of space>");
                    return false;
                }
                opnd_1 = args[2];
            }

            else{
                System.out.println("Sub Protocols: BACKUP, RESTORE, DELETE, SPACE");
                return false;
            }


        }
        return true;
    }
}
