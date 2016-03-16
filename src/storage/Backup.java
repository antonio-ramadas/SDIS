package storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Semaphore;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Backup {

    /**
     * Maximum number of requests the semaphore can allow to access at the same time
     */
    private static final int MAX_AVAILABLE = 1;
    /**
     * The chunks is accessible to all of the threads, so a semaphore is required to keep the data consistent
     */
    private final Semaphore chunksSem = new Semaphore(MAX_AVAILABLE, true);

    /**
     * This map will keep track of stored chunks.
     * The key is the file id. Each key can have multiple values.
     * Each value is a chunk.
     */
    private Map<String,Vector<Chunk>> chunks = new HashMap<String,Vector<Chunk>>();

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
                Files.walk(Paths.get("files")).forEach(filePath -> {
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

}
