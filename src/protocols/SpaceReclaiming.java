package protocols;

import communication.Connection;
import message.Message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class SpaceReclaiming implements Connection {

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public SpaceReclaiming(Message message) {
        this.message = message;
    }
    /**
     * TODO: Melhoramento (Igual ao File Deletion)
     * Cada vez que o servidor "acorda", manda removed de todos os ficheiros.
     * Caso não receba nenhuma mensagem putchunk significa que é seguro apagar.
     * Se receber, responde e não apaga.
     */

    @Override
    public void handleReceived() {

    }

    @Override
    public void send() {

    }
}
