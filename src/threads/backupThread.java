package threads;

import communication.Server;
import console.MessageCenter;
import storage.Chunk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by Rui on 01/04/2016.
 */
public class backupThread implements Runnable {

    private File file;
    private int replicationDegree;

    public backupThread(File file, int replicationDegree){
        this.file = file;
        this.replicationDegree = replicationDegree;
    }

    @Override
    public void run() {

        String fileID = sha256(file.getName() + file.lastModified() + Server.getInstance().getId());

        try {
            byte[] fileData = loadFile(file);
            int numChunks = fileData.length/ 64000 + 1; //Colocar variável global
            MessageCenter.output(file.getName() + " slitted into " + numChunks + " chunks.");
            ByteArrayInputStream stream = new ByteArrayInputStream(fileData);
            byte[] streamConsumer = new byte[6400]; //Colocar variável global

            for (int i = 0; i < numChunks; i++){

                byte[] chunkData;

                if (i == numChunks - 1 && fileData.length % 64000 == 0){
                    chunkData = new byte[0];
                }
                else {
                    int numBytesRead = stream.read(streamConsumer, 0, streamConsumer.length);

                    chunkData = Arrays.copyOfRange(streamConsumer, 0, numBytesRead);
                }

                Chunk chunk = new Chunk(Integer.toString(i), fileID, Integer.toString(replicationDegree), chunkData);

                Thread t = new Thread(new backupChunkThread(chunk));
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        } catch (FileNotFoundException e) {
            MessageCenter.error("File not fount");
        }
    }

    public static final String sha256(String str) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            byte[] hash = sha.digest(str.getBytes(StandardCharsets.UTF_8));

            StringBuffer hexStringBuffer = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1)
                    hexStringBuffer.append('0');

                hexStringBuffer.append(hex);
            }

            return hexStringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static final byte[] loadFile(File file) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(file);

        byte[] data = new byte[(int) file.length()];

        try {
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
