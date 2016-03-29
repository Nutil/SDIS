import java.io.File;

/**
 * Protocol to delete a file across the backup system
 */
public class DeleteFileProtocol extends Thread {
    private Peer peer;
    private String fileName;

    public DeleteFileProtocol (Peer peer, String fileName){
        this.peer = peer;
        this.fileName = fileName;
    }

    public void run() {
        File f = peer.getLocalFile(fileName);

        byte[] chunk = new byte[Constants.chunkSize];
        String hashedFileName = Constants.sha256(fileName);

    }
}
