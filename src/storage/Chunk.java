package storage;

import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Chunk {
    /**
     * Default minimum value of the replications.
     */
    private final static int DEFAULT_MINIMUM_VALUE = 0;

    /**
     * The minimum number allowed of replications of the chunk.
     */
    private int minimumReplication = DEFAULT_MINIMUM_VALUE;

    /**
     * The chunk data. 0 <= data.size() <= 64KB
     * If null, data needs to be read from the storage (file system).
     */
    private byte[] data = null;

    /**
     * Variable used in the Chunk restore protocol
     */
    private boolean restored = false;

    /**
     * Variable used in the Chunk restore protocol
     * to name to decide if the enhancement should be used
     */
    private boolean enhacementBR = false;

    /**
     * Variable used int the chunk backup subprotocol
     * for the space reclaiming subprotocol
     */
    private boolean required = false;

    /**
     * Chunk's id.
     */
    private String id;

    /**
     * ID of the file which the chunk belongs.
     */
    private String fileId;

    /**
     * ID of the sender of the chunk
     */
    private String senderId;

    /**
     * Stores the servers' id without replications
     */
    private Vector<String> replications = new Vector<String>();

    /**
     * Maximum number of requests the semaphore can allow to access at the same time
     */
    private static final int MAX_AVAILABLE = 1;

    /**
     * The chunk controls the access through this semaphore.
     * It's used to avoid dirty reading/writing.
     */
    private final Semaphore chunkSem = new Semaphore(MAX_AVAILABLE, true);


    /**
     * Constructor of the Chunk class.
     * @param id chunk's id
     * @param fileId file's id
     */
    public Chunk(String id, String fileId) {
        this.id = id;
        this.fileId = fileId;
    }

    /**
     * Constructor of the chunk. It misses the data.
     * @param id id of the chunk
     * @param fileId id of the file whose chunk belongs
     * @param minimumDegree minimum replication this chunk allow
     */
    public Chunk(String id, String fileId, String minimumDegree) {
        this(id, fileId);
        setReplications(minimumDegree);
    }

    /**
     * Constructor of the chunk. It misses replications numbers.
     * @param id chunk's id
     * @param fileId file's id
     * @param data chunk's data
     */
    public Chunk(String id, String fileId, byte[] data) {
        this(id, fileId);
        setData(data);
    }

    /**
     * Constructor with all the information required
     * @param chunkNo chunk id (it's the same as its numbering)
     * @param fileId file id of the chunk
     * @param replicationDeg minimum replication degree required by the initiator peer
     * @param data data of the chunk
     */
    public Chunk(String chunkNo, String fileId, String replicationDeg, byte[] data) {
        this(chunkNo, fileId, replicationDeg);
        this.data = data.clone();
    }

    /**
     * Constructor with all the information required
     * @param chunkId chunk id (it's the same as its numbering)
     * @param fileId file id of the chunk
     * @param minimumReplication_str minimum replication degree required by the initiator peer
     * @param senderId_srt id of the sender of the chunk
     */
    public Chunk(String chunkId, String fileId, String minimumReplication_str, String senderId_srt) {
        this(chunkId, fileId, minimumReplication_str);
        senderId = senderId_srt;
    }

    /**
     * Constructor with all the information required
     * @param chunkNo chunk id (it's the same as its numbering)
     * @param fileId file id of the chunk
     * @param replicationDeg minimum replication degree required by the initiator peer
     * @param senderId id of the sender of the chunk
     * @param data data of the chunk
     */
    public Chunk(String chunkNo, String fileId, String replicationDeg, String senderId, byte[] data) {
        this(chunkNo, fileId, replicationDeg, data);
        this.senderId = senderId;
    }

    /**
     * Checks if this chunk can be deleted
     * @return true if this chunk has enough copies spread across the peers, otherwise returns false.
     */
    public boolean canBeDeleted() {
        boolean deletable = false;
        acquire();
        deletable = minimumReplication > 1 && replications.size() >= minimumReplication;
        release();
        return deletable;
    }

    /**
     * Add the foreign server id in the replications vector if it
     * doesn't exists.
     * @param foreignServerId the server id to be added
     */
    public void addReplication(String foreignServerId) {
        if (!exists(foreignServerId)) {
            acquire();
            replications.add(foreignServerId);
            release();
        }
    }

    /**
     * Check if a server id is already stored
     * @param foreignServerId the server id to be checked
     * @return true if it exists, false otherwise.
     */
    private boolean exists(String foreignServerId) {
        acquire();

        for (String server : replications) {
            if (server.equals(foreignServerId)) {
                release();
                return true;
            }
        }

        release();
        return false;
    }

    /**
     * Release the semaphore chunkSem
     */
    private void release() {
        chunkSem.release();
    }

    /**
     * Check if all the information of the chunk is already stored.
     * @return true if all the information is present.
     */
    public boolean isComplete() {
        boolean completed = false;
        acquire();
        completed = data != null && minimumReplication > DEFAULT_MINIMUM_VALUE;
        release();

        return completed;
    }

    /**
     * Sets the replications numbers
     * @param minimumDegree minimum replications of the chunk
     *
     */
    public void setReplications(String minimumDegree) {
        acquire();
        minimumReplication = Integer.parseInt(minimumDegree);
        release();
    }

    /**
     * Acquire the semaphore chunkSem
     */
    private void acquire() {
        try {
            chunkSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getMinimumReplication() {
        int minimum = 0;
        acquire();
        minimum = minimumReplication;
        release();
        return minimum;
    }

    public String getId() {
        acquire();
        String chunkId = id;
        release();
        return chunkId;
    }

    public String getFileId() {
        acquire();
        String file = fileId;
        release();
        return file;
    }

    public byte[] getData() {
        acquire();
        byte[] bytes = data.clone();
        release();
        return bytes;
    }

    public void setData(byte[] data) {
        acquire();
        this.data = data.clone();
        release();
    }

    public boolean wasRestored() {
        acquire();
        boolean wasIt = restored;
        release();
        return wasIt;
    }

    public void setRestored(boolean restored) {
        acquire();
        this.restored = restored;
        release();
    }

    public boolean isEnhancementBR() {
        acquire();
        boolean enhacement = enhacementBR;
        release();
        return enhacement;
    }

    public void setEnhancementBR(boolean enhancementBR) {
        acquire();
        this.enhacementBR = enhancementBR;
        release();
    }

    public boolean wasRequired() {
        acquire();
        boolean wasIt = required;
        release();
        return wasIt;
    }

    public void setRequired(boolean required) {
        acquire();
        this.required = required;
        release();
    }

    /**
     * Removes a given peer id from the list of replications
     * @param senderId id of the peer
     */
    public void removePeer(String senderId) {
        acquire();
        replications.remove(senderId);
        release();
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
