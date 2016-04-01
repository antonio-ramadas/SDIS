package threads;

import communication.Server;
import console.MessageCenter;
import message.Message;
import protocols.ChunkRestore;
import source.Peer;
import storage.Backup;
import storage.Chunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Rui on 01/04/2016.
 */
public class restoreThread implements Runnable {

    private File file;

    public restoreThread(File file){
        this.file = file;
    }


    @Override
    public void run() {
        // confirmar se é assim
        String fileID = backupThread.sha256(file.getName() + file.lastModified() + Server.getInstance().getId());

        if (Backup.getInstance().isFileStored(fileID)){
            int numChunks = 0; //confirmar se a função de contar chunks existe.

            /*
            Preparar inicio

            chunks HashMap<String, ArrayList<Chunk>> chunks

            if (!chunks.containsKey(fileID))
                chunks.put(fileID, new ArrayList<Chunk>());
            */

            // Requesting chunks

            ArrayList<Chunk> chunks = new ArrayList<Chunk>();

            for (int i = 0; i < numChunks; i++) {
                Message m = new Message("GETCHUNK", "1.0", Server.getInstance().getId(), fileID, Integer.toString(i));
                ChunkRestore CR = new ChunkRestore(m);
                CR.send();

                // CR handler ???????????????????????????????????????????
                Chunk chunk = new Chunk("", "");
                chunks.add(chunk);
                // ??????????????????????????????????????????????????????
            }

            //parar de receber
            //chunks.remove(fileID);

            			/*
			 * Restoring file data
			 */
            byte[] fileData = new byte[0];

            for (int i = 0; i < numChunks; i++) {
                Chunk chunkI = null;

                for (Chunk chunk : chunks) {
                    if (Integer.parseInt(chunk.getId()) == i) { // Confirmar se isto esta certo ?????????????????????
                        chunkI = chunk;
                        break;
                    }
                }

                if (chunkI == null)
                    MessageCenter.error("Error! Missing file chunk.");

                fileData = concatBytes(fileData, chunkI.getData());
            }

            try {
                saveRestoredFile(file.getName(), fileData);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            MessageCenter.error("The requested file can not be restored.");
        }

    }

    public static byte[] concatBytes(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;

        byte[] c = new byte[aLen + bLen];

        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static final void saveRestoredFile(String fileName, byte[] data)
            throws IOException {
        FileOutputStream out = new FileOutputStream(fileName); // ADICIONAR PATH ???????????????????????????????????
        out.write(data);
        out.close();
    }
}
