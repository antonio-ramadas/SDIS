package message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Message {
    /**
     * Each message has a header.
     * The header contains the message specification.
     */
    private Header header = new Header();

    /**
     * Each message has a header and may have a body.
     * The body contains the message data.
     */
    private Body body = new Body();

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
