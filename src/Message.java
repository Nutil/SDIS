import java.util.Arrays;

/**
 * Created by Diogo Guarda on 16/03/2016.
 */
public class Message {

    private Header header;
    private byte[] body;

    public Message(byte[] bytesReceived){
        int i = 0;
        while(i < bytesReceived.length - 3){
            if(bytesReceived[i] == Header.CR && bytesReceived[i + 1] == Header.LF
                    && bytesReceived[i + 2] == Header.CR && bytesReceived[i + 3] == Header.LF)
                break;
        }
        String messageHeader = new String (Arrays.copyOfRange(bytesReceived,0,i));
        header = new Header(messageHeader);
        if(i+4 >= bytesReceived.length) {
            body = null;
        }
        else {
            body = Arrays.copyOfRange(bytesReceived, i + 4, bytesReceived.length);
        }
    }

    public Message(Header header, byte[] body){
        this.header = header;
        this.body = body;
    }
}
