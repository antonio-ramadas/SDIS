package protocols;

import communication.Connection;
import communication.Server;
import console.MessageCenter;
import message.Header;
import message.Message;
import message.MessageTypes;
import storage.Backup;
import storage.Chunk;

/**
 * Created by Antonio on 06-03-2016.
 */
public class SpaceReclaiming implements Connection {

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * Delta time to wait
     */
    private final static int DELTA = 100;

    /**
     * Maximum sleep time before replying
     */
    private final static int SLEEP_TIME = 401;

    /**
     * Maximum sleep time of sleep before checks the chunk
     */
    private final static int WAIT_TIME = 30001;

    /**
     * Version of the protocol
     */
    private static final String VERSION = "2.0";

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public SpaceReclaiming(Message message) {
        this.message = message;
    }
    /**
     * Melhoramento (Igual ao File Deletion)
     * Cada vez que o servidor "acorda", manda removed de todos os ficheiros.
     * Caso não receba nenhuma mensagem putchunk significa que é seguro apagar.
     * Se receber, responde e não apaga.
     */

    @Override
    public void handleReceived() {
        Chunk chunk = Backup.getInstance().getChunkThreadSafe(message.getHeader().getFileId(),
                message.getHeader().getChunkNo());
        
        if (chunk == null) {
        	return;
        }

        if (Backup.getInstance().isStored(chunk)) {
            chunk = Backup.getInstance().getChunkThreadSafe(message.getHeader().getFileId(),
                    message.getHeader().getChunkNo());

            chunk.removePeer(message.getHeader().getSenderId());

            if (!chunk.canBeDeleted()) {
                chunk.setRestored(false);
                sleep((int) ((Math.random()*1000)%SLEEP_TIME));
                if (!chunk.wasRestored()) {
                    startBackUpProtocol(chunk);
                    return;
                }
                sleep(WAIT_TIME + (int) ((Math.random()*10000)%10000));
                if (chunk != null && !chunk.canBeDeleted()) {
                    startBackUpProtocol(chunk);
                }
            }
        }
    }

    /**
     * Calls the chunk backup subprotocol to compensate the removal from a peer
     * @param chunk chunk to backed up
     */
    private void startBackUpProtocol(Chunk chunk) {
        Message msg = new Message("PUTCHUNK", VERSION, chunk.getSenderId(), chunk.getFileId(),
                chunk.getId(), Integer.toString(chunk.getMinimumReplication()), chunk.getData());

        MessageCenter.output("Space Reclaiming protocol started the chunk backup subprotocol with the message: " + msg);
        ChunkBackup cb = new ChunkBackup(msg);
        cb.send();
    }

    @Override
    public void send() {
        Chunk chunk = Backup.getInstance().getChunkThreadSafe(message.getHeader().getFileId(),
                message.getHeader().getChunkNo());
        chunk.setRestored(false);

        Server.getInstance().send(MessageTypes.REMOVED, message.getHeader().bytify());
        MessageCenter.output("Message sent: " + message);

        sleep(SLEEP_TIME + DELTA);

        if (chunk.wasRestored()) {
            //send message
            Header header = new Header("STORED", VERSION, Server.getInstance().getId(),
                    chunk.getFileId(), chunk.getId());
            Server.getInstance().send(MessageTypes.STORED, header.bytify());
            MessageCenter.output("STORED message sent for the chunk " + chunk.getId() +
                    " of the file id " + chunk.getFileId());
        }
    }

    /**
     * Sleeps the thread for time ms
     * @param time time in ms
     */
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
