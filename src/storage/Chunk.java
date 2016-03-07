package storage;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Chunk {
    /**
     * The total number of replications of the chunk.
     */
    private int totalReplications;

    /**
     * The minimun number allowed of replications of the chunk.
     */
    private int minimunReplication;

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

    public int getTotalReplications() {
        return totalReplications;
    }

    public void setTotalReplications(int totalReplications) {
        this.totalReplications = totalReplications;
    }

    public int getMinimunReplication() {
        return minimunReplication;
    }

    public void setMinimunReplication(int minimunReplication) {
        this.minimunReplication = minimunReplication;
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
        this.data = data;
    }
}
