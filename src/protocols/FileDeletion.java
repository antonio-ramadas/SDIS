package protocols;

import communication.Connection;
import communication.Server;
import console.MessageCenter;
import message.Message;
import message.MessageTypes;
import storage.Backup;
import storage.Chunk;

/**
 * Created by Antonio on 06-03-2016.
 */
public class FileDeletion implements Connection{

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * Maximum number of messages allowed to be sent
     */
    private final static int MAX_NUMBER_MSG = 2;

    /**
     * Maximum sleep time (1 second)
     */
    private final static int SLEEP_TIME = 1000;

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public FileDeletion(Message message) {
        this.message = message;
    }
    /**
     * Melhoramento (Igual ao Space Reclaiming)
     * Cada vez que o servidor "acorda", manda removed de todos os ficheiros.
     * Caso não receba nenhuma mensagem putchunk significa que é seguro apagar.
     * Se receber, responde e não apaga.
     */

    @Override
    public void handleReceived() {
        if (Backup.getInstance().isFileStored(message.getHeader().getFileId())) {
            Backup.getInstance().deleteFile(message.getHeader().getFileId());
        }
    }

    @Override
    public void send() {
        byte[] messageArray = message.getHeader().bytify();

        for (int i = 0; i < MAX_NUMBER_MSG; i++) {
            Server.getInstance().send(MessageTypes.DELETE, messageArray);
            MessageCenter.output("Message " + i + "out of " + MAX_NUMBER_MSG + " sent: " + message);
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
