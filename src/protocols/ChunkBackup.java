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
public class ChunkBackup implements Connection {

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * Maximum sleep time
     */
    private static final int SLEEP_TIME = 400 + 1;

    /**
     * Version of the protocol
     */
    private static final String VERSION = "2.0";

    /**
     * Initial timeout for the PUTCHUNK's messages (1 second)
     */
    private static final int INITIAL_TIMEOUT = 1000;

    /**
     * Maximum number of messages allowed to be sent
     */
    private static final int MAX_NUMBER_MSG = 5;

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public ChunkBackup(Message message) {
        this.message = message;
    }

    /**
     * Melhoramento
     * No tempo aleatório que espera pode ir "espreitando" se o número de pedidos já transmitidos satisfaz
     * o número mínimo. Se satisfazer, não há necessidade de também guardar. Se não satisfizer,
     * fazer o procedimento normal.
     */

    @Override
    public void handleReceived() {
        if (MessageTypes.PUTCHUNK == message.getMessageType()) {
            Chunk chunk = new Chunk(message.getHeader().getChunkNo(), message.getHeader().getFileId(),
                    message.getHeader().getReplicationDeg(), message.getHeader().getSenderId(), message.getBody().getData());

            processPutChunkMsg(chunk);
        } else {
            //it's a store message
            if (Backup.getInstance().addPeerBackingUpMyChunk(message.getHeader())) {
                //there's a peer storing my message
                MessageCenter.output("One peer is storing my chunk");
            } else {
                Backup.getInstance().addPeerStore(message.getHeader());
            }
        }
    }

    @Override
    public void send() {
        int timeout = INITIAL_TIMEOUT;
        Backup.getInstance().setNewChunkBackUp(message.getHeader().getFileId(), message.getHeader().getChunkNo());
        int minimum = Integer.parseInt(message.getHeader().getReplicationDeg());
        int count = MAX_NUMBER_MSG;
        byte[] messageArray = message.compose();

        //while there isn't a minimum number of replications
        //and the number of messages sent doesn't exceed the maximum
        while (Backup.getInstance().getMyChunksBackingUpCount(message.getHeader()) < minimum && count > 0) {
            count--;
            Server.getInstance().send(MessageTypes.PUTCHUNK, messageArray);
            MessageCenter.output("Sent: " + message);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeout *= 2;
        }

        if (Backup.getInstance().getMyChunksBackingUpCount(message.getHeader()) < minimum) {
            MessageCenter.error("Failed to back up: " + message);
        }

        Backup.getInstance().removeChunkBackingUp(message.getHeader());
    }

    /**
     * Do the procedure required for a PUTCHUNK received message.
     * @param chunk chunk received
     */
    private void processPutChunkMsg(Chunk chunk) {

        //can't store chunks from messages started by its own
        if (!Server.getInstance().sameId(message.getHeader().getSenderId()) &&
                Backup.getInstance().addChunk(chunk)) {
            //there's enough space to store and it doesn't exist
            sleep();
            if (chunk.canBeDeleted()) {
                Backup.getInstance().deleteChunk(chunk);
            } else {
                sendStoredMsg(chunk);

                Backup.getInstance().writeChunk(chunk);
                MessageCenter.output("Chunk " + chunk.getId() +
                        " of the file id " + chunk.getFileId() +
                        " stored in the system");
            }
        } else if (Backup.getInstance().isStored(chunk)) {
            sleep();
            sendStoredMsg(chunk);
            Chunk c = Backup.getInstance().getChunkThreadSafe(chunk.getFileId(), chunk.getId());
            if (c != null) {
            	c.setRestored(true);
            }
        }
    }

    /**
     * Sleeps the thread for a random time between 0..SLEEP_TIME
     */
    private void sleep() {
        try {
            Thread.sleep(((int) (Math.random()*1000))%SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send STORE message if the chunk is stored
     * @param chunk chunk to be acknowledge by broadcast
     */
    private void sendStoredMsg(Chunk chunk) {
        //send message
        Header header = new Header("STORED", VERSION, Server.getInstance().getId(),
                chunk.getFileId(), chunk.getId());
        Server.getInstance().send(MessageTypes.STORED, header.bytify());
        MessageCenter.output("STORED message sent for the chunk " + chunk.getId() +
                " of the file id " + chunk.getFileId());
    }
}
