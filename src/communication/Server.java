package communication;

import storage.Chunk;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Server {
    /**
     * Server's id.
     */
    private String id;

    /**
     * Channel of the Multicast Channel, the control channel
     */
    private Channel MC_Channel;
    /**
     * Channel of the Multicast Data Channel
     */
    private Channel MDB_Channel;
    /**
     * Channel of the Multicast Data Recovery channel
     */
    private Channel MDR_Channel;

    /**
     * Maximum number of requests the semaphore can allow to access at the same time
     */
    private static final int MAX_AVAILABLE = 1;

    /**
     * Number of requests is the server still dealing with.
     */
    private int number_threads_running = 0;
    /**
     * The number_threads_running is accessible to all of the threads,
     * so a semaphore is required to keep the data consistent
     */
    private final Semaphore nThreadsSem = new Semaphore(MAX_AVAILABLE, true);

    /**
     * Only one sever is allowed.
     */
    private static Server ourInstance = new Server();

    public static Server getInstance() {
        return ourInstance;
    }

    /**
     * Each channel is handled by a separated thread
     */
    private class MyThread implements Runnable {

        private Channel channel;

        /**
         * Constructor of the thread. It requires the channel's information.
         * @param channel1 channel that will be handled by this thread
         */
        public MyThread(Channel channel1) {
            // store parameter for later user
            channel = channel1;
            try {
                channel.getSocket().joinGroup(channel.getAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Loop to listen the channel. Each meaningful message is handled by another separate thread.
         * This method has a loop to check the channels. In case of data received, it'll call the handler.
         */
        public void run() {
            //loop

            //increase for each request
            incNumberThreads();

            //calls the request handler
        }
    }

    /**
     * Start the connection for the 3 channels previously configured.
     * This method will start the connection handler.
     */
    public void start() {
        Runnable mc = new MyThread(MC_Channel);
        Thread mc_thread = new Thread(mc);
        mc_thread.start();

        Runnable mdb = new MyThread(MDB_Channel);
        Thread mdb_thread = new Thread(mdb);
        mdb_thread.start();

        Runnable mdr = new MyThread(MDR_Channel);
        Thread mdr_thread = new Thread(mdr);
        mdr_thread.start();

        try {
            mc_thread.join();
            mdb_thread.join();
            mdr_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new channel. The channel initializes the socket.
     * @param s Type of Channel (MC, MDB, MDR)
     * @param ip IP of the channel
     * @param port Port of the channel
     */
    public void createChannel(Sockets s, String ip, String port) {
        Channel socket = new Channel(s, ip, port);
        switch (s) {
            case MULTICAST_CHANNEL:
                MC_Channel = socket;
                break;
            case MULTICAST_DATA_CHANNEL:
                MDB_Channel = socket;
                break;
            case MULTICAST_DATA_RECOVERY:
                MDR_Channel = socket;
                break;
        }
    }

    /**
     * For each request a new thread is created. So, it's necessary to keep
     * track of them with precaution.
     */
    private void incNumberThreads() {
        try {
            nThreadsSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        number_threads_running++;
        nThreadsSem.release();
    }

    /**
     * For each request a new thread is created and when it ends the count needs to be updated.
     * So, it's necessary to keep track of them with precaution.
     */
    public void decNumberThreads() {
        try {
            nThreadsSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        number_threads_running--;
        if (number_threads_running < 0) {
            number_threads_running = 0;
        }
        nThreadsSem.release();
    }

    public void setId(String id1) {
        id = id1;
    }
}
