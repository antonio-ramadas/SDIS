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
     * The size of the chunk. 0 <= size <= 64KB
     */
    private int size;

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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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
}
