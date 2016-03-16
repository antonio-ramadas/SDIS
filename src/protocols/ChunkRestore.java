package protocols;

import communication.Connection;
import message.Message;

/**
 * Created by Antonio on 06-03-2016.
 */
public class ChunkRestore implements Connection {

    /**
     * Message stored of the protocol
     */
    Message message = null;

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
     * TODO: Melhoramento
     * Deixar para quando o projeto estiver feito.
     * Mandar CHUNK para a rede sem o body para os peers fazerem a contagem,
     * Mandar pessoalmente o CHUNK com o body para o que pediu GETCHUNK
     */

    @Override
    public void handleReceived() {

    }
}
