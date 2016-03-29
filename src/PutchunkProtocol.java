import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Created by Luís on 25/03/2016.
 */
public class PutchunkProtocol extends Thread {
    private Peer peer;
    private String fileName;
    private int repDegree;

    public PutchunkProtocol(Peer peer, String fileName, int repDegree){
        this.peer = peer;
        this.fileName = fileName;
        this.repDegree = repDegree;
    }

    public void run(){
        //Get File to be saved.
        File f = peer.getLocalFile(fileName);

        byte[] chunk = new byte[Constants.chunkSize];
        String hashedFileName = Constants.sha256(fileName);

        //Divide file into chunks and save them individually
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            int chunkNumber = 0;
            int resends = 0;
            int timeToSleep = 100;
            int chunkRepDegree = 0;
            int bytesRead;
            while((bytesRead = bis.read(chunk)) > -1 ) {
                FileInfo.getInstance().addInfo(hashedFileName,chunkNumber,0,repDegree);
                chunk = Arrays.copyOf(chunk,bytesRead);
                System.out.println("Read first chunk. Sending chunk with size: " + chunk.length);
                for(; resends < 5 && chunkRepDegree < repDegree; resends++) {
                    chunkRepDegree = FileInfo.getInstance().getInfo(hashedFileName,chunkNumber).getActualRepDegree();
                    Header messageHeader = new Header("PUTCHUNK", Constants.PROTOCOL_VERSION, peer.getServerID(), hashedFileName, chunkNumber, repDegree);
                    Message msg = new Message(messageHeader, chunk);
                    DatagramPacket requestPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, peer.getMdbAddress(), peer.getMdbPort());
                    peer.getMDB().send(requestPacket);

                    //Await peer responses
                    Thread.sleep(timeToSleep *(long) Math.pow(1, (double)resends));
                }

                resends = 0;
                chunkRepDegree = 0;
                chunkNumber++;
                chunk = new byte[Constants.chunkSize];
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
