package com.protocols;

import com.peer.Peer;
import com.utils.ChunksInfo;
import com.utils.Constants;
import com.utils.Header;
import com.utils.Message;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

/**
 * Created by Diogo Guarda on 30/03/2016.
 */
public class RemoveChunkProtocol implements Runnable {

    private String fileId;
    private int chunkNo;
    private Peer peer;

    public RemoveChunkProtocol(String fileId, int chunkNo, Peer peer) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.peer = peer;
    }

    @Override
    public void run() {
        File dir = new File(Constants.FILE_PATH + peer.getServerID(),fileId);
        File file = new File(dir,chunkNo+ Constants.FILE_EXTENSION);
        file.delete();
        ChunksInfo.getInstance().updateInfo(fileId,chunkNo);
        Header header = new Header("REMOVED", Constants.PROTOCOL_VERSION,peer.getServerID(),fileId,chunkNo, Constants.REP_DEGREE_IGNORE);
        Message msg = new Message(header,null);
        DatagramPacket packet = new DatagramPacket(msg.getBytes(),msg.getBytes().length,peer.getMcAddress(), peer.getMcPort());
        try {
            peer.getMC().send(packet);
        } catch (IOException e) {
            System.err.println("Error: Could not send REMOVED MESSAGE");
        }

    }
}
