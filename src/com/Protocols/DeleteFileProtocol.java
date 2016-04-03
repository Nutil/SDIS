package com.protocols;

import com.peer.Peer;
import com.utils.*;

import java.io.File;
import java.net.DatagramPacket;

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


        ChunksInfo filesTable = ChunksInfo.getInstance();

        for(int i = 0; i < 5; i++){
            byte[] emptyBody = null;
            Header messageHeader = new Header("DELETE", Constants.PROTOCOL_VERSION, peer.getServerID(), hashedFileName, Constants.CHUNK_NO_IGNORE, Constants.REP_DEGREE_IGNORE);
            Message msg = new Message(messageHeader, emptyBody);
            DatagramPacket requestPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, peer.getMcAddress(), peer.getMcPort());
            try {
                peer.getMC().send(requestPacket);
                Thread.sleep((long) (1000 * Math.pow(2,i)));
            } catch (Exception e) {
                System.err.println("Error: Delete message not sent");
            }
            if(ChunksInfo.getInstance().check(hashedFileName)){
                break;
            }
        }

        filesTable.removeFileEntries(hashedFileName);
        MyFiles.getInstance().removeInfo(fileName);

    }
}
