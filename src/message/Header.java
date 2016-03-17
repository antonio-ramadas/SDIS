package message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Header {

    private final static byte CR = 0x0D;
    private final static byte LF = 0x0A;

    /**
     * This is the type of the message.
     * Is encoded as a variable length sequence of ASCII characters.
     */
    private String messageType;

    /**
     * This is the version of the protocol.
     * It is a three ASCII char sequence with the format <n>'.'<m>, where <n> and <m> are the ASCII codes of digits.
     */
    private String version;

    /**
     * This is the id of the server that has sent the message.
     * This is encoded as a variable length sequence of ASCII digits.
     */
    private String senderId;

    /**
     * This is the file identifier for the backup service.
     * It is supposed to be obtained by using the SHA256 cryptographic hash function.
     * As its name indicates its length is 256 bit, i.e. 32 bytes, and should be encoded as a 64 ASCII character sequence.
     * The encoding is as follows: each byte of the hash value is encoded by the two ASCII characters corresponding to the hexadecimal representation of that byte.
     */
    private String fileId;

    /**
     * This field together with the FileId specifies a chunk in the file.
     * It is encoded as a sequence of ASCII characters corresponding to the decimal representation of that number, with the most significant digit first.
     * The length of this field is variable, but should not be larger than 6 chars.
     */
    private String chunkNo;

    /**
     * This field contains the desired replication degree of the chunk.
     * This is a digit, thus allowing a replication degree of up to 9.
     */
    private String replicationDeg;

    /**
     * Constructor. Receives all the information through the arguments.
     * @param messageType type of message
     * @param version version of the message
     * @param senderId id of the sender
     * @param fileId id of the file
     * @param chunkNo number (id) of the chunk
     * @param replicationDeg degree of replication of the chunk
     */
    public Header(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDeg) {
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
    }

    /**
     * Converts the header to an array of bytes following the specification
     * @return header converted into byte array
     */
    public byte[] bytify() {
        String head = messageType + " " + version + " " + senderId + " " + fileId + " " + chunkNo + " " + replicationDeg + " ";

        int size = head.length();
        byte[] data = new byte[size+4];
        System.arraycopy(head.getBytes(), 0, data, 0, head.getBytes().length);

        data[size] = CR; size++;
        data[size] = LF; size++;
        data[size] = CR; size++;
        data[size] = LF;

        return data;
    }

    @Override
    public String toString() {
        return "Header: " + messageType + " " + version + " " + senderId + " " + fileId + " " + chunkNo + " " + replicationDeg;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(String chunkNo) {
        this.chunkNo = chunkNo;
    }

    public String getReplicationDeg() {
        return replicationDeg;
    }

    public void setReplicationDeg(String replicationDeg) {
        this.replicationDeg = replicationDeg;
    }
}
