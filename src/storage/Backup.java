package storage;

import console.MessageCenter;
import message.Header;
import message.Message;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
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
     * Maximum allowed space used by the chunks.
     */
    private int maxAllowedUsage = Integer.MAX_VALUE;

    /**
     * Occupied space by the chunks
     */
    private int currentUsage = 0;

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
     * Name of the chunk I'm backing up.
     * Syntax (the same of the filenames):
     * [file_id] [space] [chunk_id]
     */
    private String myChunkBackingUp = "";
    /**
     * Id of the peers that are backing up my chunk
     */
    private Vector<String> myChunkCount = new Vector<String>();

    /**
     * Only one file explorer handler is allowed.
     */
    private static Backup ourInstance = new Backup();

    public static Backup getInstance() {
        return ourInstance;
    }

    /**
     * Get the size of the vector myChunkCount
     * @return myChunkCount's size
     */
    public int getChunkCount() {
        int size = 0;
        acquire();
        size = myChunkCount.size();
        release();

        return size;
    }

    /**
     * Set new chunk to back up. Just sets the new name
     * and clears the vector
     * @param file file id
     * @param chunk chunk id
     */
    public void setNewChunkBackUp(String file, String chunk) {
        acquire();
        myChunkBackingUp = file + " " + chunk;
        myChunkCount = new Vector<String>();
        release();
    }

    /**
     * Check if the chunk of the header is stored.
     * If it is, then the peer's id is stored
     * @param head header of the message
     */
    public void addPeerStore(Header head) {
        if (isStored(head.getFileId(), head.getChunkNo())) {
            acquire();
            chunks.get(head.getFileId()).get(head.getChunkNo()).addReplication(head.getSenderId());
            release();
        }
    }

    /**
     * Check if is my chunk that is being compared.
     * @param id id to be compared
     * @return true if it's the same, false otherwise
     */
    private boolean isItMyChunk(String id) {
        boolean isMine = false;
        acquire();
        isMine = myChunkBackingUp.equals(id);
        release();

        return isMine;
    }

    /**
     * Check if the header received is due to my chunk
     * being backed up.
     * @param head header of the message
     * @return true if this header belongs to my chunk, flase otherwise
     */
    public boolean addPeerBackingUpMyChunk(Header head) {
        if (isItMyChunk(head.getFileId() + " " + head.getChunkNo())) {
            acquire();
            if (!myChunkCount.contains(head.getSenderId())) {
                myChunkCount.add(head.getSenderId());
            }
            release();
            return true;
        }

        return false;
    }

    /**
     * Adds a chunk with careful.
     * First checks if already exists and then if there is enough space available
     * @param c chunk to be added
     * @return true if is stored, false otherwise
     */
    public boolean addChunk(Chunk c) {
        if (isStored(c) || !canStore(c.getData().length)) {
            return false;
        }

        add(c);

        return true;
    }

    /**
     * Add a chunk to the backup storage (in the volatile memory only).
     * @param c chunk to be stored
     */
    private void add(Chunk c) {
        acquire();

        if (chunks.get(c.getFileId()) == null) {
            chunks.put(c.getFileId(), new HashMap<String,Chunk>());
        }

        chunks.get(c.getFileId()).put(c.getId(), c);

        release();
    }

    /**
     * Checks if a chunk is already stored.
     * The comparision is made with the chunk's id and file id
     * @param file file id
     * @param chunk chunk id
     * @return true if it's stored, otherwise false
     */
    private boolean isStored(String file, String chunk) {
        acquire();

        boolean exists = chunks.get(file) != null &&
                chunks.get(file).get(chunk) != null;

        release();

        return exists;
    }

    /**
     * Checks if a chunk is already stored.
     * The comparision is made with the chunk's id and file id
     * @param c chunk to be checked if is stored
     * @return true if it's stored, otherwise false
     */
    public boolean isStored(Chunk c) {
        acquire();

        boolean exists = chunks.get(c.getFileId()) != null &&
                chunks.get(c.getFileId()).get(c.getId()) != null;

        release();

        return exists;
    }

    /**
     * Acquires a place at the semaphore chunksSem.
     */
    private void acquire() {
        try {
            chunksSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Releases a place at the semaphore chunksSem.
     */
    private void release() {
        chunksSem.release();
    }

    /**
     * Checks if a chunk can be stored given its size
     * @param size size of the chunk to be stored
     * @return true if enough space to store, false otherwise
     */
    private boolean canStore(int size) {
        boolean enoughSpace = false;
        acquire();

        enoughSpace = currentUsage + size <= maxAllowedUsage;

        release();

        return enoughSpace;
    }

    /**
     * Initializes the hash map with the chunks of the system.
     */
    public void start() {
        File dir = new File(PATH + "chunks");
        Boolean success = dir.mkdir();
        if (success) {
            MessageCenter.output("Directory Created!");
        } else {
            acquire();
            readChunks();
            countSpace();
            release();
        }
    }

    /**
     * Count the current space used by the chunks
     */
    private void countSpace() {
        for (Map.Entry<String, Map<String,Chunk> > fileIds : chunks.entrySet()) {
            for (Map.Entry<String,Chunk> chunkIds : fileIds.getValue().entrySet()) {
                byte[] data = chunkIds.getValue().getData();
                if (data != null) {
                    currentUsage += data.length;
                } else {
                    MessageCenter.error("Bad usage storing the chunk with id: " + chunkIds.getValue().getId()
                            + " and file id: " + chunkIds.getValue().getFileId());
                }
            }
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

    /**
     * Delete a chunk from the hash map.
     * @param chunk chunk to be deleted
     */
    public void deleteChunk(Chunk chunk) {
        acquire();
        chunks.get(chunk.getFileId()).remove(chunk.getId());
        release();
    }

    /**
     * Writes all the important information file in the system
     * @param chunk chunk to be stored
     */
    public void writeChunk(Chunk chunk) {
        writeDataFile(chunk);
        writeInfoFile(chunk);
    }

    /**
     * Create a file with the information and stores it.
     * Only the minimum replication degree is stored.
     * @param chunk chunk to be written to a file
     */
    private void writeInfoFile(Chunk chunk) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(PATH + "chunks/" + createFileName(chunk) + "." + FILE_INFO_EXTENSION, "UTF-8");
            writer.print(chunk.getMinimumReplication());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the byte array of the chunk to the file
     * @param chunk chunk to be stored in the file
     */
    private void writeDataFile(Chunk chunk) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(PATH + "chunks/" + createFileName(chunk) + "." + FILE_DATA_EXTENSION);
            fos.write(chunk.getData());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a filename based on the chunk.
     * Syntax: [file_id] [space] [chunk_id]
     * @param chunk chunk to create the filename
     * @return filename of the chunk
     */
    private String createFileName(Chunk chunk) {
        return chunk.getFileId() + " " + chunk.getId();
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
