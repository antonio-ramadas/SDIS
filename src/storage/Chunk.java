package storage;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Chunk {

    /**
     * Max size of a chunk. This isn't the real size.
     * The real max size is 64KB. Here is used 70KB just
     * to prevent errors in the future.
     */
    public static int MAX_SIZE = 70000;

    /**
     * The minimun number allowed of replications of the chunk.
     */
    private int minimumReplication;

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
