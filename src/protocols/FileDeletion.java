package protocols;

import communication.Connection;
import message.Message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class FileDeletion implements Connection{

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public FileDeletion(Message message) {
        this.message = message;
    }
    /**
     * TODO: Melhoramento (Igual ao Space Reclaiming)
     * Cada vez que o servidor "acorda", manda removed de todos os ficheiros.
     * Caso não receba nenhuma mensagem putchunk significa que é seguro apagar.
     * Se receber, responde e não apaga.
     */

    @Override
    public void handleReceived() {

    }
}
