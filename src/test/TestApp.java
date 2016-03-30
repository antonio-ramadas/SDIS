package test;

import java.io.*;
import java.net.*;
import console.MessageCenter;

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

        MessageCenter.output(response);

        out.close();
        in.close();

        socket.close();
    }

    private static boolean validateArgs(String[] args) {
        if (args.length < 3 || args.length > 4){
            MessageCenter.output("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            return false;
        }
        else{
            peer_ap = args[0];
            String[] peer_ap_split = peer_ap.split(":");
            address = peer_ap_split[0];
            if (!validStringToInt(peer_ap_split[1])){
                return false;
            }
            port = Integer.parseInt(peer_ap_split[1]);

            sub_protocol = args[1];

            if (sub_protocol.equals("BACKUP")){
                if (args.length != 4) {
                    MessageCenter.output("Usage: java TestApp <address:port> BACKUP <file path> <replication degree>");
                    return false;
                }
                if (!validFilePath(args[2])) {
                    return false;
                }
                opnd_1 = args[2];
                if (!validStringToInt(args[3])){
                    return false;
                }
                opnd_2 = args[3];
            }
            else if (sub_protocol.equals("RESTORE")){
                if (args.length != 3) {
                    MessageCenter.output("Usage: java TestApp <address:port> RESTORE <file path>");
                    return false;
                }
                if (!validFilePath(args[2])) {
                    return false;
                }
                opnd_1 = args[2];
            }
            else if (sub_protocol.equals("DELETE")){
                if (args.length != 3) {
                    MessageCenter.output("Usage: java TestApp <address:port> DELETE <file path>");
                    return false;
                }
                if (!validFilePath(args[2])) {
                    return false;
                }
                opnd_1 = args[2];
            }
            else if (sub_protocol.equals("SPACE")){
                if (args.length != 3) {
                    MessageCenter.output("Usage: java TestApp <address:port> SPACE <amount of space>");
                    return false;
                }
                if (!validStringToInt(args[2])){
                    return false;
                }
                opnd_1 = args[2];
            }

            else{
                MessageCenter.output("Sub Protocols: BACKUP, RESTORE, DELETE, SPACE");
                return false;
            }


        }
        return true;
    }

    private static boolean validFilePath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            MessageCenter.output(file.getAbsolutePath() + " does not exist");
            return false;
        } else if (!file.isFile()) {
            MessageCenter.output(file.getAbsolutePath() + " is not a file, it is a folder");
            return false;
        }
        return true;
    }

    private static boolean validStringToInt(String intStr) {
        try {
            Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            MessageCenter.error(intStr + " must be a valid integer");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
