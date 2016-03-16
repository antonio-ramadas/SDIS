package storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Backup {
    /**
     * This map will keep track of stored chunks.
     * The key is the file id. Each key can have multiple values.
     * Each value is a chunk.
     */
    private static Map<String,Vector<Chunk>> chunks = new HashMap<String,Vector<Chunk>>();

    /**
     * Only one file explorer handler is allowed.
     */
    private static Backup ourInstance = new Backup();

    public static Backup getInstance() {
        return ourInstance;
    }

    private Backup() {
        File dir = new File("files/chunks");
        Boolean success = dir.mkdir();
        if (success) {
            System.out.println("Directory Created!");
        } else {
            try {
                Files.walk(Paths.get("files/chunks")).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        System.out.println(filePath);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dir = new File("files/backup");
        success = dir.mkdir();
        if (success) {
            System.out.println("Directory Created!");
        } else {
            try {
                Files.walk(Paths.get("files")).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        System.out.println(filePath);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dir = new File("files/reconstruct");
        success = dir.mkdir();
        if (success) {
            System.out.println("Directory Created!");
        } else {
            try {
                Files.walk(Paths.get("files")).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        System.out.println(filePath);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Vector<Chunk>> getChunks() {
        return chunks;
    }

    public void setChunks(Map<String, Vector<Chunk>> chunks) {
        this.chunks = chunks;
    }
}
