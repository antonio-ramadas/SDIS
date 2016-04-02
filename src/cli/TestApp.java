package cli;

import communication.Server;
import console.MessageCenter;
import message.MessageTypes;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

/**
 * Created by Antonio on 01-04-2016.
 */
public class TestApp {
    /**
     * Protocol of the message
     */
    Protocols protocol = null;

    /**
     * Arguments of the message
     */
    String[] args = null;

    /**
     * ID of the peer
     */
    public int peerId = 0;

    private static final int DEFAULT_RMI_PORT = 1099;

    public static void main(String[] args) {

        TestApp testApplication = new TestApp();
        TestApp rmi = testApplication.getMessage(args);

        if (rmi == null) {
            testApplication.printUsage();
            return;
        }

        try {
            LocateRegistry.createRegistry(DEFAULT_RMI_PORT);
        } catch (RemoteException e) {
            MessageCenter.output("Registry already exists");
        }

        try {
            Registry registry = LocateRegistry.getRegistry(null);
            RMI_Message stub = (RMI_Message) registry.lookup(args[CLI_Arguments.PEER.ordinal()]);
            stub.handlerRMI(rmi.protocol, rmi.args);
            MessageCenter.output("Sent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the arguments given and check for errors
     * @param args arguments received on execution
     * @return TestApp if successful, null in case of error
     */
    private TestApp getMessage(String[] args) {
        if (args.length < 3) {
            return null;
        }

        TestApp rmi = new TestApp();
        rmi.peerId = Integer.parseInt(args[CLI_Arguments.PEER.ordinal()]);

        Protocols msg = getType(args[CLI_Arguments.PROTOCOL.ordinal()]);
        rmi.protocol = msg;

        switch (msg) {
            case BACKUP:
                return parseBackup(rmi, args);
            case RESTORE:
            case RESTOREENH:
            case DELETE:
            case RECLAIM:
                return parseNonBackup(rmi, args);
            default:
                return null;
        }
    }

    /**
     * Parse the arguments to the message to be sent with RMI.
     * As all protocols except for the Backup can be parsed as the same, this method does it for all.
     * @param rmi object to be fulfilled
     * @param args arguments received
     * @return object to be sent, null in case of error
     */
    private TestApp parseNonBackup(TestApp rmi, String[] args) {
        if (args.length != 3) {
            return null;
        }

        rmi.args = new String[1];
        rmi.args[0] = args[CLI_Arguments.OP1.ordinal()];

        return rmi;
    }

    /**
     * Parse the arguments to the message to be sent with RMI.
     * @param rmi object to be fulfilled
     * @param args arguments received
     * @return object to be sent, null in case of error
     */
    private TestApp parseBackup(TestApp rmi, String[] args) {
        if (args.length != 4) {
            return null;
        }

        rmi.args = Arrays.copyOfRange(args, CLI_Arguments.OP1.ordinal(), CLI_Arguments.OP2.ordinal()+1);

        return rmi;
    }

    /**
     * Parse the argument given to the protocol
     * @param arg argument received
     * @return string parsed to enum
     */
    private Protocols getType(String arg) {
        switch(arg.toUpperCase()) {
            case "BACKUP":
            case "BACKUPENH":
                return Protocols.BACKUP;
            case "RESTORE":
                return Protocols.RESTORE;
            case "RESTOREENH":
                return Protocols.RESTOREENH;
            case "DELETE":
            case "DELETEENH":
                return Protocols.DELETE;
            case "RECLAIM":
            case "RECLAIMENH":
                return Protocols.RECLAIM;
            default:
                return null;
        }
    }

    /**
     * Prints on the console the usage of the application. Prints to the standard error output.
     */
    private void printUsage() {
        MessageCenter.error("Usage:\n" +
                "java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>\n" +
                "<peer_ap>\n" +
                "Is the local peer access point. This depends on the implementation. (Check the next section)\n" +
                "<sub_protocol>\n" +
                "Is the sub protocol being tested, and must be one of: BACKUP, RESTORE, DELETE, RECLAIM." +
                "In the case of enhancements, you must append the substring ENH at the end of the" +
                "respecive subprotocol, e.g. BACKUPENH\n" +
                "<opnd_1>\n" +
                "Is either the path name of the file to backup/restore/delete, for the respective 3 subprotocols," +
                "or the amount of space to reclaim. In the latter case, the peer should execute" +
                "the RECLAIM protocol, upon deletion of any chunk.\n" +
                "<opnd_2>\n" +
                "This operand is an integer that specifies the desired replication degree and" +
                "applies only to the backup protocol (or its enhancement)");
    }
}
