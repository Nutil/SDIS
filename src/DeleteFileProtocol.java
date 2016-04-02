import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Hashtable;
import java.util.Iterator;

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

        String hashedFileName = MyFiles.getInstance().getFileId(fileName);

        FileInfo filesTable = FileInfo.getInstance();

        filesTable.removeFileEntries(hashedFileName);
        MyFiles.getInstance().removeInfo(fileName);

        byte[] emptyBody = null;

        Header messageHeader = new Header("DELETE", Constants.PROTOCOL_VERSION, peer.getServerID(), hashedFileName, Constants.CHUNK_NO_IGNORE, Constants.REP_DEGREE_IGNORE);
        Message msg = new Message(messageHeader, emptyBody);
        DatagramPacket requestPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, peer.getMcAddress(), peer.getMcPort());
        try {
            peer.getMC().send(requestPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
