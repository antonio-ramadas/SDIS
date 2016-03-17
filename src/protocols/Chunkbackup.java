package protocols;

import communication.Connection;
import message.Message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class ChunkBackup implements Connection {

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public ChunkBackup(Message message) {
        this.message = message;
    }

    /**
     * TODO: Melhoramento
     * No tempo aleatório que espera pode ir "espreitando" se o número de pedidos já transmitidos satisfaz
     * o número mínimo. Se satisfizer, não há necessidade de também guardar. Se não satisfazer,
     * fazer o procedimento normal.
     */

    @Override
    public void handleReceived() {

    }
}
