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
            Registry registry = LocateRegistry.getRegistry(null);
            registry.bind(Server.getInstance().getId(), stub);

            MessageCenter.output("Peer ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

}
