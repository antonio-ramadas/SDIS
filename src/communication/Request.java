package communication;

import console.MessageCenter;
import message.Message;
import message.MessageTypes;
import protocols.ChunkBackup;
import protocols.ChunkRestore;
import protocols.FileDeletion;
import protocols.SpaceReclaiming;

import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Created by Antonio on 16-03-2016.
 */
public class Request {

    /**
     * The request.
     */
    private byte[] data;

    /**
     * The address of the peer who sent the request.
     */
    private String address;
    /**
     * The port of the peer who sent the request.
     */
    private int port;

    /**
     * Constructor of the request. Starts a new thread to deal with the request.
     * The objective is to be the fastest as it can gets so a message isn't lost.
     * @param packet The request received in a form of packet. It'll be decomposed.
     */
    public Request(DatagramPacket packet) {
        data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        address = new String(packet.getAddress().getHostAddress());
        port = packet.getPort();

        Runnable request = new MyThread();
        Thread request_thread = new Thread(request);
        request_thread.start();
    }

    /**
     * Each request is handled by a separated thread
     */
    private class MyThread implements Runnable {

        /**
         * Each request is dealt with on a asynchronous call
         */
        public void run() {

            Message message = new Message(data);
            if (message.decompose()) {
                //the message is from another peer
                if (!Server.getInstance().sameId(message.getHeader().getSenderId()))
                {
                    MessageCenter.output("Message successfully received and decomposed: " + message);
                    chooseProtocol(message);
                }
            } else {
                MessageCenter.error("Message received with errors!");
            }

            //the last final instruction
            //it means that the count of the number of active threads will decrease
            Server.getInstance().decNumberThreads();
        }
    }

    /**
     * Choose the protocol of the message.
     * Call its handler.
     * @param message message decomposed received from the channels
     */
    private void chooseProtocol(Message message) {
        switch (message.getMessageType()) {
            case PUTCHUNK:
            case STORED:
                ChunkBackup backup = new ChunkBackup(message);
                backup.handleReceived();
                break;
            case GETCHUNK:
                ChunkRestore restore1 = new ChunkRestore(message, address, port);
                restore1.handleReceived();
                break;
            case CHUNK:
                ChunkRestore restore2 = new ChunkRestore(message);
                restore2.handleReceived();
                break;
            case DELETE:
                FileDeletion fileDeletion = new FileDeletion(message);
                fileDeletion.handleReceived();
                break;
            case REMOVED:
                SpaceReclaiming spaceReclaiming = new SpaceReclaiming(message);
                spaceReclaiming.handleReceived();
                break;
        }
    }
}
