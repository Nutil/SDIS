import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * Created by Luís on 25/03/2016.
 */
public class PutFileProtocol extends Thread {
    private Peer peer;
    private String fileName;
    private int repDegree;

    public PutFileProtocol(Peer peer, String fileName, int repDegree){
        this.peer = peer;
        this.fileName = fileName;
        this.repDegree = repDegree;
    }

    public void run(){
        //Get File to be saved.
        File f = peer.getLocalFile(fileName);

        try {
            BasicFileAttributes attributes = Files.readAttributes(f.toPath(),BasicFileAttributes.class);
            MyFiles.getInstance().addFileInfo(fileName,attributes);
        } catch (IOException e) {
            System.err.println("Error: could not acess files attributes! BACKUP aborted");
        }

        byte[] chunk = new byte[Constants.chunkSize];

        String hashedFileName = MyFiles.getInstance().getFileId(fileName);

        //Divide file into chunks and save them individually
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            int chunkNumber = 0;
            int bytesRead;
            while((bytesRead = bis.read(chunk)) > -1 ) {
                FileInfo.getInstance().addInfo(hashedFileName,chunkNumber,0,repDegree);
                chunk = Arrays.copyOf(chunk,bytesRead);
                final int finalChunkNumber = chunkNumber;
                final byte[] finalChunk = chunk;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int chunkRepDegree = 0;
                        int timeToSleep = 1000;
                        int tries = 0;
                        for(; tries < 5 && chunkRepDegree < repDegree; tries++) {
                            chunkRepDegree = FileInfo.getInstance().getInfo(hashedFileName, finalChunkNumber).getActualRepDegree();
                            Header messageHeader = new Header("PUTCHUNK", Constants.PROTOCOL_VERSION, peer.getServerID(), hashedFileName, finalChunkNumber, repDegree);
                            Message msg = new Message(messageHeader, finalChunk);
                            DatagramPacket requestPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, peer.getMdbAddress(), peer.getMdbPort());

                            //Await peer responses
                            try {
                                peer.getMDB().send(requestPacket);
                                Thread.sleep(timeToSleep *(long) Math.pow(2, (double)tries));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                chunkNumber++;
                chunk = new byte[Constants.chunkSize];
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
