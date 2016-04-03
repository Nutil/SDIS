package com.protocols;

import com.peer.Peer;
import com.utils.Constants;
import com.utils.Header;
import com.utils.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Diogo Guarda on 28/03/2016.
 */
public class RestoreFileProtocol implements Runnable{
    private Peer peer;
    private String fileName;
    private String fileId;
    private int totalChunks;
    private ConcurrentHashMap<Integer,byte[]> chunks;

    public RestoreFileProtocol(Peer peer, String filename, String fileId, int totalChunks) {
        this.peer = peer;
        this.fileId = fileId;
        this.totalChunks = totalChunks;
        this.fileName = filename;
        this.chunks = new ConcurrentHashMap<>();
    }


    public void addBytes(int chunkNo, byte[] fileData){
        chunks.put(chunkNo,fileData);
        if (chunks.size() == totalChunks){
            peer.removeRestoreRequest(this);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    createFile();
                }
            }).start();

        }
    }

    private void createFile() {
        File dir = new File(Constants.FILE_PATH + peer.getServerID() + File.separator + "restored");
        File file = new File(dir, this.fileName);

        try {
            dir.mkdirs();
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            for (int i = 0; i < chunks.size(); i++){
                byte[] data = chunks.get(i);
                out.write(data);
            }
            out.close();
            System.out.println("File restored! Check: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("A file with the same name exist in your filesystem");
        }
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public void run() {
        for (int chunkNo = 0; chunkNo < totalChunks; chunkNo++){
            final int finalChunkNo = chunkNo;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Header requestHeader = new Header("GETCHUNK", Constants.PROTOCOL_VERSION, peer.getServerID(),fileId, finalChunkNo, Constants.REP_DEGREE_IGNORE);
                    Message request = new Message(requestHeader,null);
                    DatagramPacket packet = new DatagramPacket(request.getBytes(),request.getBytes().length,peer.getMcAddress(),peer.getMcPort());
                    try {
                        peer.getMC().send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }
}
