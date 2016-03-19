package communication;

import message.Message;

/**
 * Created by Antonio on 16-03-2016.
 */
public interface Connection {
    /**
     * Handle the receive message and makes the respective process
     */
    void handleReceived();

    /**
     * Send the message. Each protocol should follow its procedure
     */
    void send();
}
