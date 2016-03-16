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

    //wait for the interface to implement this method
    //void send();
}
