package cli;

import communication.Server;
import message.MessageTypes;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Antonio on 01-04-2016.
 */
public interface RMI_Message extends Remote {
    /**
     * Handler of the peer for the messages of the RMI
     * @param protocol protocol to be used
     * @param args arguments for the protocol
     * @throws RemoteException
     */
    void handlerRMI(Protocols protocol, String[] args) throws RemoteException;
}
