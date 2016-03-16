/**
 * Created by Diogo Guarda on 16/03/2016.
 */
public class Header {

    public static final byte CR = 0xD;
    public static final byte LF = 0xA;
    private String messageType;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replicationDegree;

    public Header(String message){
        String[] fields = message.split(" ");
        messageType = fields[0];
        version = fields[1];
        senderId = Integer.parseInt(fields[2]);
        fileId = fields[3];
        chunkNo = Integer.parseInt(fields[4]);
        replicationDegree = Integer.parseInt(fields[5]);
    }

    public String getMessageType() {
        return messageType;
    }

    public String getVersion() {
        return version;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

}
