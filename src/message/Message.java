package message;

import console.MessageCenter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Message {

    /**
     * The regex pattern of the message.
     */
    private final static String PATTERN = "((?i)\\w+) (\\d\\.\\d) (\\w+) ([A-Fa-f0-9]{64}) (\\d{1,6}) (\\d) \r\n\r\n";

    /**
     * Each message has a header.
     * The header contains the message specification.
     */
    private Header header = null;

    /**
     * Each message has a header and may have a body.
     * The body contains the message data.
     */
    private Body body = null;

    /**
     * The message of the message.
     */
    private byte[] message = null;

    /**
     * Type of message. (PUTCHUNK, STORED, ...)
     */
    private MessageTypes messageType = null;

    /**
     * Constructor of the message header and body.
     * @param messageType Type of message (PUTCHUNK, STORED, ...)
     * @param version Version of the message (x.y)
     * @param senderId ID of the server
     * @param fileId ID of the file
     * @param chunkNo Number of the chunk
     * @param replicationDeg Degree of replication
     * @param data Body of the message
     */
    public Message(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDeg, byte[] data) {
        this(messageType, version, senderId, fileId, chunkNo, replicationDeg);
        body = new Body(data);
    }

    /**
     * Constructor of the message header.
     * @param messageType Type of message (PUTCHUNK, STORED, ...)
     * @param version Version of the message (x.y)
     * @param senderId ID of the server
     * @param fileId ID of the file
     * @param chunkNo Number of the chunk
     * @param replicationDeg Degree of replication
     */
    public Message(String messageType, String version, String senderId, String fileId, String chunkNo, String replicationDeg) {
        header = new Header(messageType, version, senderId, fileId, chunkNo, replicationDeg);
    }

    /**
     * Create an object with the message string
     * @param message the message received (including header and body)
     */
    public Message(byte[] message) {
        this.message = message;
    }

    /**
     * Fills the object with the message received.
     * @return is the message received without errors?
     */
    public boolean decompose() {

        //create a Pattern object
        Pattern r = Pattern.compile(PATTERN);

        //convert the byte array to string
        String message_string = new String(message);

        //now create matcher object
        Matcher m = r.matcher(message_string);

        if (m.find()) {
            structurize(m);
            parseType();
        } else {
            //the message does not follow the pattern
            MessageCenter.error("Message failed to be parsed.");
            return false;
        }

        return true;
    }

    /**
     * Converts the type of message from string to enum element.
     */
    private void parseType() {
        String type = header.getMessageType();

        if (type.equalsIgnoreCase("PUTCHUNK")) {
            messageType = MessageTypes.PUTCHUNK;
            return;
        }
        if (type.equalsIgnoreCase("STORED")) {
            messageType = MessageTypes.STORED;
            return;
        }

        if (type.equalsIgnoreCase("GETCHUNK")) {
            messageType = MessageTypes.GETCHUNK;
            return;
        }
        if (type.equalsIgnoreCase("CHUNK")) {
            messageType = MessageTypes.CHUNK;
            return;
        }

        if (type.equalsIgnoreCase("DELETE")) {
            messageType = MessageTypes.DELETE;
            return;
        }

        if (type.equalsIgnoreCase("REMOVED")) {
            messageType = MessageTypes.REMOVED;
        }
    }

    /**
     * The message is composed to an array of bytes
     * @return Array of bytes of the message
     */
    public byte[] compose() {
        byte[] headerArray = header.bytify();
        if (body != null) {
            byte[] bodyArray = body.getData();
            byte[] messageArray = new byte[headerArray.length + bodyArray.length];

            System.arraycopy(headerArray, 0, messageArray, 0, headerArray.length);
            System.arraycopy(bodyArray, 0, messageArray, headerArray.length, bodyArray.length);

            return messageArray;
        }

        return headerArray;
    }

    @Override
    public String toString() {
        if (body != null) {
            return header.toString() + " " +  body.toString();
        }
        return header.toString();
    }

    /**
     * Store the message in its variables
     * @param m the match of the message
     */
    private void structurize(Matcher m) {
        header = new Header(m.group(MatchGroup.MESSAGE_TYPE.ordinal()),
                m.group(MatchGroup.VERSION.ordinal()),
                m.group(MatchGroup.SENDER_ID.ordinal()),
                m.group(MatchGroup.FILE_ID.ordinal()),
                m.group(MatchGroup.CHUNK_NUMBER.ordinal()),
                m.group(MatchGroup.REPLICATION_DEGREE.ordinal()));

        if (m.end() <  message.length)
            body = new Body(Arrays.copyOfRange(message,m.end(),message.length));
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public MessageTypes getMessageType() {
        return messageType;
    }
}
