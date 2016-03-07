package communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Server {
    /**
     * Server's id.
     */
    private String id;

    /**
     * IP of the Multicast Channel, the control channel
     */
    private String MC_IP;
    /**
     * Port of theMulticast Channel, the control channel
     */
    private String MC_PORT;

    /**
     * IP of the Multicast Data Channel
     */
    private String MDB_IP;
    /**
     * Port of the Multicast Data Channel
     */
    private String MDB_PORT;

    /**
     * IP of the Multicast Data Recovery channel
     */
    private String MDR_IP;
    /**
     * Port of the Multicast Data Recovery channel
     */
    private String MDR_PORT;

    /**
     * Only one sever is allowed.
     */
    private static Server ourInstance = new Server();

    public static Server getInstance() {
        return ourInstance;
    }

    /**
     * Initializes the socket as a multicast socket
     * @param socket Multicast Socket Object
     * @param port Address of the port
     */
    private void initializeSocket(MulticastSocket socket, String port) {
        try {
            socket = new MulticastSocket(Integer.parseInt(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Each channel is handled by a separated thread
     */
    private class MyThread implements Runnable {

        private MulticastSocket socket;
        private InetAddress address;
        private Sockets channel;

        /**
         * Constructor of the thread. It requires the channel's information.
         * @param type channel that will be handled by this thread
         * @param ip channel's ip
         * @param port channel's port
         */
        public MyThread(Sockets type, String ip, String port) {
            // store parameter for later user
            channel = type;
            initializeSocket(socket, port);
            try {
                address = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        /**
         * Loop to listen the channel. Each meaningful message is handled by another separate thread.
         */
        public void run() {
            for (int i = 0; i < 2; i++) {
                System.out.println(channel);
                try {
                    Thread.sleep((long) Math.random()%1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Start the connection for the 3 channels previously configured.
     * This method will start the connection handler.
     */
    public void start() {
        Runnable mc = new MyThread(Sockets.MULTICAST_CHANNEL, MC_IP, MC_PORT);
        Thread mc_thread = new Thread(mc);
        mc_thread.start();

        Runnable mdb = new MyThread(Sockets.MULTICAST_DATA_CHANNEL, MDB_IP, MDB_PORT);
        Thread mdb_thread = new Thread(mdb);
        mdb_thread.start();

        Runnable mdr = new MyThread(Sockets.MULTICAST_DATA_RECOVERY, MDR_IP, MDR_PORT);
        Thread mdr_thread = new Thread(mdr);
        mdr_thread.start();

        try {
            mc_thread.join();
            mdb_thread.join();
            mdr_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("passou!");
    }

    /**
     * This method has a loop to check the channels. In case of data received, it'll call the handler.
     */
    private void run(Sockets type) {

    }

    public void setMC(String ip, String port) {
        MC_IP = ip;
        MC_PORT = port;
    }

    public void setMDB(String ip, String port) {
        MDB_IP = ip;
        MDB_PORT = port;
    }

    public void setMDR(String ip, String port) {
        MDR_IP = ip;
        MDR_PORT = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id1) {
        id = id1;
    }
}
