import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
            i++;
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

    public byte[] getBytes(){
        byte[] headerBytes = header.getBytes();
        byte[] CRLF = new byte[]{Header.CR, Header.LF, Header.CR, Header.LF};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(headerBytes);
            outputStream.write(CRLF);
            if(body != null){
                outputStream.write(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public byte[] getBody() {
        return body;
    }

    public Header getHeader() {
        return header;
    }
}
