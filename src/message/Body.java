package message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Body {

    /**
     * The data from the message body. 0 <= data.size() <= 64KB
     * If null, data needs to be read from the storage (file system).
     */
    private byte[] data = null;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
