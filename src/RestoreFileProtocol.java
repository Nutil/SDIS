import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Diogo Guarda on 28/03/2016.
 */
public class RestoreFileProtocol{
    private Peer peer;
    private int chunkNo;
    private String fileName;
    private String fileId;
    private ConcurrentHashMap<Integer,byte[]> chunks;

    public RestoreFileProtocol(Peer peer, String filename, String fileId) {
        this.peer = peer;
        this.fileId = fileId;
        this.chunkNo = -1;
        this.fileName = filename;
        this.chunks = new ConcurrentHashMap<>();
    }

    public void getNextChunk(){
        chunkNo++;
        Header requestHeader = new Header("GETCHUNK", Constants.PROTOCOL_VERSION, peer.getServerID(),fileId,chunkNo,Constants.REP_DEGREE_IGNORE);
        Message request = new Message(requestHeader,null);
        DatagramPacket packet = new DatagramPacket(request.getBytes(),request.getBytes().length,peer.getMcAddress(),peer.getMcPort());
        try {
            peer.getMC().send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addBytes(int chunkNo, byte[] fileData){
        chunks.put(chunkNo,fileData);
        if (fileData.length != Constants.chunkSize){
            peer.removeRestoreRequest(this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createFile();
                }
            }).start();

        }
        else{
            this.getNextChunk();
        }
    }

    private void createFile() {
        File dir = new File(Constants.FILE_PATH + peer.getServerID() + File.separator + "restored");
        File file = new File(dir, this.fileName);

        try {
            System.out.println("BOA CENA");
            dir.mkdirs();
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            for (int i = 0; i < chunks.size(); i++){
                byte[] data = chunks.get(i);
                out.write(data);
            }
            out.close();
        } catch (IOException e) {
            System.out.println("A file with the same name exist in your filesystem");
            e.printStackTrace();
        }
    }

    public String getFileId() {
        return fileId;
    }
}
