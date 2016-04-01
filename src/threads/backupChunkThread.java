package threads;

import storage.Chunk;

/**
 * Created by Rui on 01/04/2016.
 */
public class backupChunkThread implements Runnable {

    private Chunk chunk;

    public backupChunkThread(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void run() {
        //Enviar mensagem ----------------------------------------------------------------
    }
}
