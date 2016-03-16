package communication;

import console.MessageCenter;
import message.Message;

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
                MessageCenter.output("Message successfully received: " + message);
            } else {
                MessageCenter.error("Message received with errors!");
            }

            //the last final instruction
            //it means that the count of the number of active threads will decrease
            Server.getInstance().decNumberThreads();
        }
    }
}
