import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by Diogo Guarda on 16/03/2016.
 */
public class Header {

    public static final byte CR = 0xD;
    public static final byte LF = 0xA;
    private static final String[] types = {"PUTCHUNK","STORED", "GETCHUNK", "CHUNK", "DELETE", "REMOVED"};
    private String messageType;
    private String version;
    private int senderId;
    private String fileId;
    private int chunkNo;
    private int replicationDegree;

    public Header(String message) throws  IllegalArgumentException{
        String[] fields = message.split("\\s+");

        if(fields.length < 4 || fields.length > 6){
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        messageType = fields[0];
        version = fields[1];
        senderId = Integer.parseInt(fields[2]);
        fileId = fields[3];
        if(fields.length >= 5)
            chunkNo = Integer.parseInt(fields[4]);
        else
            chunkNo = Constants.CHUNK_NO_IGNORE;

        if(fields.length == 6)
            replicationDegree = Integer.parseInt(fields[5]);
        else
            replicationDegree = Constants.REP_DEGREE_IGNORE;

        if(!Arrays.asList(types).contains(messageType))
            throw new IllegalArgumentException("Invalid <MessageType>");

        Pattern r = Pattern.compile("[0-9]*.[0-9]");
        if(!r.matcher(version).matches())
            throw new IllegalArgumentException("Invalid <Version>");

    }

    public Header(String messageType, String version, int senderId, String fileId, int chunkNo, int replicationDegree)
            throws IllegalArgumentException{
        if(!Arrays.asList(types).contains(messageType))
            throw new IllegalArgumentException("Invalid <MessageType>");
        Pattern r = Pattern.compile("[0-9]*.[0-9]");
        if(!r.matcher(version).matches())
            throw new IllegalArgumentException("Invalid <Version>");

        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;

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

    public byte[] getBytes(){
        return toString().getBytes();
    }

    public String toString(){
        String msg = messageType + " " + version + " " + senderId + " " + fileId + " ";
        if(chunkNo != Constants.CHUNK_NO_IGNORE){
            msg += chunkNo + " ";
        }
        if(replicationDegree != Constants.REP_DEGREE_IGNORE){
            msg += replicationDegree + " ";
        }
        return msg;
    }

}
