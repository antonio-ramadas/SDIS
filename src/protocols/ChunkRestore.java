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
public class ChunkRestore implements Connection {

    /**
     * Message stored of the protocol
     */
    Message message = null;

    /**
     * If true then the enhancement is used.
     * If false, then the standard protocol is used.
     * This only valid when sending. When receiving,
     * the enhancement is always used. Which means, discard
     * the body of the enhancement.
     */
    private boolean useEnhancement = false;

    /**
     * Maximum sleep time
     */
    private static final int SLEEP_TIME = 400 + 1;

    /**
     * Version of the normal protocol
     */
    private static final String VERSION_NORMAL = "1.0";

    /**
     * Version of the enhancement protocol
     */
    private static final String VERSION_ENHANCEMENT = "1.9";

    /**
     * Initial timeout for the PUTCHUNK's messages (1 second)
     */
    private static final int INITIAL_TIMEOUT = 1000;

    /**
     * Maximum number of messages allowed to be sent
     */
    private static final int MAX_NUMBER_MSG = 5;

    /**
     * The address of the peer who sent the request.
     */
    private String senderAddress;
    /**
     * The port of the peer who sent the request.
     */
    private int senderPort;

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     */
    public ChunkRestore(Message message) {
        this.message = message;
        useEnhancement = false;
    }

    /**
     * Constructor of Chunk Backup Protocol
     * @param message message received from the channels
     * @param enhancement use enhancement
     */
    public ChunkRestore(Message message, boolean enhancement) {
        this.message = message;
        useEnhancement = enhancement;
    }

    /**
     * Constructor of Chunk Backup Protocol given the sender address and port.
     * The address and port will be useful for the enhancement.
     * @param message message received from the channels
     * @param address sender address (string style)
     * @param port sender port
     */
    public ChunkRestore(Message message, String address, int port) {
        this(message);
        this.senderAddress = address;
        this.senderPort = port;
    }

    /**
     * Melhoramento
     * Mandar CHUNK para a rede sem o body para os peers fazerem a contagem,
     * Mandar pessoalmente o CHUNK com o body para o que pediu GETCHUNK
     */

    @Override
    public void handleReceived() {
        if (MessageTypes.GETCHUNK == message.getMessageType()) {
            //if can't store there's no need to wait
            Chunk chunk = new Chunk(message.getHeader().getChunkNo(), message.getHeader().getFileId());
            if (Backup.getInstance().isStored(chunk)) {
                chunk = Backup.getInstance().getChunkThreadSafe(message.getHeader().getFileId(),
                        message.getHeader().getChunkNo());
                chunk.setRestored(false);
                chunk.setEnhancementBR(message.getHeader().getVersion().equals(VERSION_ENHANCEMENT));
                sleep();
                if (!chunk.wasRestored()) {
                    sendChunkMsg(chunk);
                }
            }
        } else {
            //it's a chunk message
            Chunk chunk = new Chunk(message.getHeader().getChunkNo(), message.getHeader().getFileId());
            if (Backup.getInstance().isStored(chunk)) {
                Backup.getInstance().getChunkThreadSafe(chunk.getFileId(), chunk.getId()).setRestored(true);
                return;
            }
            if (message.getHeader().getVersion().equals(VERSION_NORMAL) &&
                    Backup.getInstance().isItMyChunkRestored(chunk)) {
                MessageCenter.error("Received asked chunk: " + message);
                chunk.setData(message.getBody().getData());
                Backup.getInstance().addChunkRestored(chunk);
            }
        }
    }

    /**
     * Send a chunk message
     * @param chunk chunk to be sent
     */
    private void sendChunkMsg(Chunk chunk) {
        String version = VERSION_NORMAL;
        if (chunk.isEnhancementBR()) {
            useEnhancement = true;
            version = VERSION_ENHANCEMENT;
        }
        Message msg = new Message("CHUNK", version, Server.getInstance().getId(),
                chunk.getFileId(), chunk.getId());
        if (!useEnhancement) {
            msg.createBody(chunk.getData());
            Server.getInstance().send(MessageTypes.CHUNK, msg.compose());
            MessageCenter.output("Standard chunk message sent: " + msg);
        } else {
            Server.getInstance().send(MessageTypes.CHUNK, msg.compose());
            MessageCenter.output("First enhancement chunk message sent: " + msg);
            msg.getHeader().setVersion(VERSION_NORMAL);
            msg.createBody(chunk.getData());
            Server.getInstance().send(MessageTypes.CHUNK, msg.compose(), senderAddress, senderPort);
            MessageCenter.output("Second enhancement chunk message sent: " + msg);
        }
    }

    @Override
    public void send() {
        int timeout = INITIAL_TIMEOUT;
        int count = MAX_NUMBER_MSG;
        byte[] messageArray = message.compose();
        Backup.getInstance().createNewRestore(message.getHeader());
        String id = message.getHeader().getFileId() + " " + message.getHeader().getChunkNo();

        //while there isn't a minimum number of replications
        //and the number of messages sent doesn't exceeds the maximum
        while (!Backup.getInstance().receivedChunk(id) && count > 0) {
            count--;
            Server.getInstance().send(MessageTypes.GETCHUNK, messageArray);
            MessageCenter.output("Sent: " + message);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeout *= 2;
        }

        if (!Backup.getInstance().receivedChunk(id)) {
            MessageCenter.error("Failed to restore: " + message);
        }
    }

    /**
     * Sleeps the thread for a random time between 0..SLEEP_TIME
     */
    private void sleep() {
        try {
            Thread.sleep((long) (Math.random()%SLEEP_TIME));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
