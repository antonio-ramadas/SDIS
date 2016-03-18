package storage;

import console.MessageCenter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Antonio on 06-03-2016.
 */
public class Backup {

    /**
     * Path of the folders and files
     */
    private final static String PATH = "files/";

    /**
     * fileid + [space] + chunkid + "."
     */
    private final static String FILE_NAME_REGEX = "(\\w*) (\\w*).";
    /**
     * fileid + [space] + chunkid + ".chk"
     */
    private final static String FILE_INFO_EXTENSION = "chk";
    /**
     * fileid + [space] + chunkid + ".bin"
     */
    private final static String FILE_DATA_EXTENSION = "bin";

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
     * The key is the file id. Each key of each value is the id of the chunk.
     * Each value is a chunk.
     */
    private Map<String,Map<String,Chunk> > chunks = new HashMap<String,Map<String,Chunk> >();

    /**
     * Only one file explorer handler is allowed.
     */
    private static Backup ourInstance = new Backup();

    public static Backup getInstance() {
        return ourInstance;
    }

    /**
     * Initializes the hash map with the chunks of the system.
     */
    public void start() {
        File dir = new File(PATH + "chunks");
        Boolean success = dir.mkdir();
        if (success) {
            MessageCenter.output("Directory Created!");
            return;
        } else {
            readChunks();
        }
    }

    /**
     * Read a file of chunks
     */
    private void readChunks() {
        try {
            Files.walk(Paths.get(PATH + "chunks")).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    MessageCenter.output("Chunk found: " + filePath);
                    readNewFile(filePath);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse input of a file
     * @param filePath file to be parsed
     */
    private void readNewFile(Path filePath) {
        String fileId = null, chunkId = null;

        //the filename is the last path
        String filename = filePath.getName(filePath.getNameCount()-1).toString();
        //create a Pattern object
        Pattern r = Pattern.compile(FILE_NAME_REGEX);
        //now create matcher object
        Matcher m = r.matcher(filename);
        if (!m.find()) {
            MessageCenter.error("Chunk file with bad syntax: " + filePath);
            return;
        }

        fileId = m.group(FilenameGroups.FILE_ID.ordinal());
        chunkId = m.group(FilenameGroups.CHUNK_ID.ordinal());

        if (isInfo(filePath)) {
            parseInfo(filePath, fileId, chunkId);
        } else if (isData(filePath)) {
            parseData(filePath, fileId, chunkId);
        } else {
            MessageCenter.error("File found, but not recognized: " + filePath);
            return;
        }
    }

    /**
     * Parse the data of the chunk of a .bin file
     * @param filePath path to the chunk file
     * @param fileId id of the file
     * @param chunkId id of the chunk
     */
    private void parseData(Path filePath, String fileId, String chunkId) {

        byte[] data = readData(filePath).clone();
        if (data == null) {
            MessageCenter.error("File found, but data read error: " + filePath);
            return;
        }

        Chunk chk = getChunk(fileId, chunkId);

        //chunk already exists?
        if (chk != null) {
            chk.setData(data);
        } else {
            //file already exists?
            if (chunks.get(fileId) == null) {
                chunks.put(fileId, new HashMap<String,Chunk>());
            }
            chunks.get(fileId).put(chunkId, new Chunk(chunkId, fileId, data));
        }
    }

    /**
     * Parse the information of the chunk of a .chk file
     * @param filePath path to the chunk file
     * @param fileId id of the file
     * @param chunkId id of the chunk
     */
    private void parseInfo(Path filePath, String fileId, String chunkId) {

        String minimumReplication_str = null;

        try (InputStream in = Files.newInputStream(filePath);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in))) {
            minimumReplication_str = reader.readLine();
            reader.close();
        } catch (IOException x) {
            x.printStackTrace();
        }

        Chunk chk = getChunk(fileId, chunkId);

        //chunk already exists?
        if (chk != null) {
            chk.setReplications(minimumReplication_str);
        } else {
            //file already exists?
            if (chunks.get(fileId) == null) {
                chunks.put(fileId, new HashMap<String,Chunk>());
            }
            chunks.get(fileId).put(chunkId, new Chunk(chunkId, fileId, minimumReplication_str));
        }
    }

    /**
     * Returns a chunk of the hash map given the keys
     * @param fileId file id of the chunk
     * @param chunkId id of the chunk
     * @return null if not found, Chunk otherwise
     */
    private Chunk getChunk(String fileId, String chunkId) {
        if (chunks.get(fileId) != null && chunks.get(fileId).get(chunkId) != null) {
            return chunks.get(fileId).get(chunkId);
        }

        return null;
    }

    /**
     * Read to a byte array the data read of the chunk file
     * @param filePath path to the chunk file
     * @return null in case of error (see Stack Trace if that happens), otherwise returns a byte array
     */
    private byte[] readData(Path filePath) {
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check if the filename holds the data
     * @param filePath file path of the file to have the name asserted
     * @return true if matches the regex of a data file
     */
    private boolean isData(Path filePath) {
        //the filename is the last path
        String filename = filePath.getName(filePath.getNameCount()-1).toString();

        return checkRegex(filename, FILE_NAME_REGEX + FILE_DATA_EXTENSION);
    }

    /**
     * Check if the filename holds the information
     * @param filePath file path of the file to have the name asserted
     * @return true if matches the regex of an information file
     */
    private boolean isInfo(Path filePath) {
        //the filename is the last path
        String filename = filePath.getName(filePath.getNameCount()-1).toString();

        return checkRegex(filename, FILE_NAME_REGEX + FILE_INFO_EXTENSION);
    }

    /**
     * Given a regex string and a string to parse, it returns whether or not
     * it fully matches
     * @param sentence string to be asserted
     * @param regex string that holds the regex expression
     * @return true on a fully match, false otherwise
     */
    private boolean checkRegex(String sentence, String regex) {
        //create a Pattern object
        Pattern r = Pattern.compile(regex);

        //now create matcher object
        Matcher m = r.matcher(sentence);

        return m.matches();
    }

    /*private Backup() {
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
    }*/

}
