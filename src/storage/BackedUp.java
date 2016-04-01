package storage;

/**
 * Created by Antonio on 31-03-2016.
 */
public class BackedUp {
    /**
     * ID of the file (SHA-256)
     */
    private String fileId;

    /**
     * Size of the file in bytes
     */
    private int size;

    /**
     * Constructor of the class BackedUp.
     * Initialize both attributes
     * @param id id of the file
     * @param size size of the file
     */
    public BackedUp(String id, int size) {
        setFileId(id);
        this.setSize(size);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
