package message;

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
    private final static String PATTERN = "((?i)\\w+) (\\d\\.\\d) (\\w+) ([A-Fa-f0-9]{64}) (\\d{1,6}) (\\d) ..";

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
    private byte[] message;

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
        } else {
            //the message does not follow the pattern
            return false;
        }

        return true;
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
}
