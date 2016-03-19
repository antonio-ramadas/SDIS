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
     * Chunk's id.
     */
    private String id;

    /**
     * ID of the file which the chunk belongs.
     */
    private String fileId;

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
     * Checks if this chunk can be deleted
     * @return true if this chunk has enough copies spread across the peers, otherwise returns false.
     */
    public boolean canBeDeleted() {
        boolean deletable = false;
        try {
            chunkSem.acquire();
            deletable = replications.size() >= minimumReplication;
            chunkSem.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return deletable;
    }

    /**
     * Add the foreign server id in the replications vector if it
     * doesn't exists.
     * @param foreignServerId the server id to be added
     */
    public void addReplication(String foreignServerId) {
        if (!exists(foreignServerId)) {
            try {
                chunkSem.acquire();
                replications.add(foreignServerId);
                chunkSem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check if a server id is already stored
     * @param foreignServerId the server id to be checked
     * @return true if it exists, false otherwise.
     */
    private boolean exists(String foreignServerId) {
        try {
            chunkSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String server : replications) {
            if (server.equals(foreignServerId)) {
                chunkSem.release();
                return true;
            }
        }

        chunkSem.release();
        return false;
    }

    /**
     * Check if all the information of the chunk is already stored.
     * @return true if all the information is present.
     */
    public boolean isComplete() {
        boolean completed = false;
        try {
            chunkSem.acquire();
            completed = data != null && minimumReplication > DEFAULT_MINIMUM_VALUE;
            chunkSem.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return completed;
    }

    /**
     * Sets the replications numbers
     * @param minimumDegree minimum replications of the chunk
     *
     */
    public void setReplications(String minimumDegree) {
        minimumReplication = Integer.parseInt(minimumDegree);
    }

    public int getMinimumReplication() {
        return minimumReplication;
    }

    public void setMinimumReplication(int minimumReplication) {
        this.minimumReplication = minimumReplication;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data.clone();
    }
}
