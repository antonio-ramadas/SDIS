package communication;

import cli.CLI_Arguments;
import console.MessageCenter;
import message.Body;
import message.Header;
import message.Message;
import message.MessageTypes;
import protocols.ChunkBackup;
import protocols.ChunkRestore;
import protocols.FileDeletion;
import protocols.SpaceReclaiming;
import storage.BackedUp;
import storage.Backup;
import storage.Chunk;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.regex.MatchResult;

import javafx.util.Pair;

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
     * Socket for the enhancement of the restore
     */
    private Channel restoreEnh;
    
    /**
     * Socket for the enhancement of the restore
     */
    private int portRestore;

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

    public String getId() {
        return id;
    }

    /**
     * Send a message to the group
     * @param type type of message
     * @param bytify byte array to be sent
     */
    public void send(MessageTypes type, byte[] bytify) {
        Channel channel = getChannel(type);
        if (channel == null) {
            MessageCenter.error("Bad type of message chosen: " + type);
            return;
        }

        try {
            nThreadsSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DatagramPacket packet = new DatagramPacket(bytify, bytify.length,
                channel.getAddress(), Integer.parseInt(channel.getPort()));
        try {
            channel.getSocket().send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nThreadsSem.release();
    }

    /**
     * Get the channel from the type of message
     * @param type type of message (PUTCHUNK, STORED, ...)
     * @return channel if found, null otherwise
     */
    private Channel getChannel(MessageTypes type) {
        switch (type) {
            //MDB
            case PUTCHUNK:
                return MDB_Channel;
            //MC
            case GETCHUNK:
            case STORED:
            case DELETE:
            case REMOVED:
                return MC_Channel;
            //MDR
            case CHUNK:
                return MDR_Channel;
        }
        return null;
    }

    /**
     * Send a message to a specific address and port
     * @param type type of message
     * @param msg message to be sent in a byte array
     * @param senderAddress address (ip) to send
     * @param senderPort port to send
     */
    public void send(MessageTypes type, byte[] msg, String senderAddress, int senderPort) {
        Channel channel = getChannel(type);
        if (channel == null) {
            MessageCenter.error("Bad type of message chosen: " + type);
            return;
        }

        try {
            nThreadsSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(msg, msg.length,
                    InetAddress.getByName(senderAddress), senderPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            channel.getSocket().send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        nThreadsSem.release();
    }

    /**
     * Initiates the backUp protocol
     * @param args arguments given to the TestApp
     */
    public void backUp(String[] args) {
        String filename = args[0];
        byte[] data = Backup.getInstance().readFile(filename);
        if (data == null) {
            MessageCenter.error("Error loading file: " + filename);
            return;
        }

        String fileId = Backup.getInstance().hashFile(filename);
        byte[] chunkData = new byte[0];
        int chunkSize = 64000;
        int numberOfChunks = data.length / chunkSize;
        int i;
        String replication = args[1];
        for (i = 0; i <= numberOfChunks; i++) {
        	
        	int max = Math.min((i+1)*chunkSize, data.length);

            chunkData = Arrays.copyOfRange(data, i*chunkSize, max);
            Message m = new Message("PUTCHUNK", "2.0", id, fileId, Integer.toString(i), replication, chunkData);

            ChunkBackup CB = new ChunkBackup(m);
            CB.send();
        }

        /*
        //too small
        if (numberOfChunks == 0) {
            chunkData = Arrays.copyOfRange(data, 0, data.length);
            Message m = new Message("PUTCHUNK", "2.0", id, fileId, Integer.toString(0), replication, chunkData);

            ChunkBackup CB = new ChunkBackup(m);
            CB.send();
        } else if (data.length % chunkSize == 0) {
            //last chunk with body size 0
            Message m = new Message("PUTCHUNK", "2.0", id, fileId, Integer.toString(i), replication, new byte[0]);

            ChunkBackup CB = new ChunkBackup(m);
            CB.send();
        }*/

        BackedUp bu = new BackedUp(fileId, data.length);
        Backup.getInstance().addFileBackedUp(filename, bu);
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
        }

        /**
         * Loop to listen the channel. Each meaningful message is handled by another separate thread.
         * This method has a loop to check the channels. In case of data received, it'll call the handler.
         */
        public void run() {
            //loop
            while (true) {
                byte buf[] = new byte[Header.MAX_SIZE + Body.MAX_SIZE];
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);

                try {
                    channel.getSocket().receive(msgPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //increase for each request
                incNumberThreads();

                //calls the request handler
                new Request(msgPacket);
            }
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

        Runnable restore = new MyThread(restoreEnh);
        Thread restore_thread = new Thread(restore);
        restore_thread.start();

        Backup.getInstance().start();

        //uncomment to wait for the threads
        /*try {
            mc_thread.join();
            mdb_thread.join();
            mdr_thread.join();
            restore_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
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

    /**
     * Compare the id of the peer with the given id.
     * This method is thread safe
     * @param id1 id to be compared
     * @return true if the ids are the same, false otherwise
     */
    public boolean sameId(String id1) {
        return getId().equals(id1);
    }

    /**
     * Initiates the deletion protocol for the file
     * @param args file name
     */
	public void delete(String[] args) {
		String filename = args[0];
		BackedUp bu = Backup.getInstance().getFileBackedUp(filename);

        Message m = new Message("DELETE", "2.0", id, bu.getFileId(), null);
        
        FileDeletion FD = new FileDeletion(m);
        FD.send();

        Backup.getInstance().removeFileBackedUp(filename);
	}

	/**
     * Initiates the reclaim protocol
     * @param args size (in bytes) to save
	 */
	public void reclaim(String[] args) {
		int sizeToReclaim = Integer.parseInt(args[0]);

		Vector<Pair<String,String> > canBeDeleted = Backup.getInstance().getChunksCanBeDeleted();
		
		for (Pair<String,String> pair : canBeDeleted) {
			Chunk chk = Backup.getInstance().getChunkThreadSafe(pair.getKey(), pair.getValue());
			Backup.getInstance().acquire();
			sizeToReclaim -= chk.getData().length;
			Backup.getInstance().release();
			
			
			Message msg = new Message("REMOVED", "2.0", id, pair.getKey(), pair.getValue());
            SpaceReclaiming sr = new SpaceReclaiming(msg);
            sr.send();
            
            Backup.getInstance().delete(pair.getKey(), pair.getValue());
            
            if (sizeToReclaim <= 0) {
            	return;
            }
		}
		
		if (sizeToReclaim > 0) {
			MessageCenter.error("Failed to free enough space");
        }
	}

	/**
	 * Initiates the restore protocol
	 * @param args name of the file
	 * @param enhancement if true then the enhancement is used, otherwise it's not used
	 */
	public void restore(String[] args, boolean enhancement) {
		String filename = args[0];
		BackedUp bu = Backup.getInstance().getFileBackedUp(filename);
		String fileId = bu.getFileId();
		int size = bu.getSize();
		
		String version = "1.0";
		String senderId = id;
		if (enhancement) {
			version = "1.9";
			senderId = Integer.toString(portRestore);
		}
		
		int numberOfChunks = size / 64000;
		if (size % 64000 != 0) {
			numberOfChunks++;
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(Backup.getInstance().getPathToRestore(filename));
			
			for (int i = 0; i < numberOfChunks; i++) {
				Message msg = new Message("GETCHUNK", version, senderId, fileId, Integer.toString(i));
				ChunkRestore CR = new ChunkRestore(msg);
				CR.send();
				
				String id = fileId + " " + Integer.toString(i);

				if (Backup.getInstance().receivedChunk(id)) {
					Chunk chk = Backup.getInstance().getChunkRestoredAndDelete(id);
					if (chk != null) {
						try {
							fos.write(chk.getData());
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
			
			fos.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the channel for the restore enhancement
	 */
	public void startRestoreEnhancement() {
		restoreEnh = new Channel();
		portRestore = restoreEnh.getSocketPort();
	}
}
