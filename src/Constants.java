import java.io.File;
import java.security.MessageDigest;

/**
 * Created by Luís on 17/03/2016.
 */
public class Constants {
    public static String PROTOCOL_VERSION = "1.0";
    public static int PACKET_BUFFER_SIZE = 65536;
    public static String FILE_PATH = System.getProperty("user.dir") + File.separator + "src" + File.separator + "Local Files" + File.separator;
    public static int chunkSize = 64000; // 64KB
    public static int delay = 400; // 64KB


    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static byte[] trim(byte[] input){
        int i = input.length;
        while (i-- > 0 && input[i] == 32) {

        }
        byte[] output = new byte[i+1];
        System.arraycopy(input, 0, output, 0, i+1);
        return output;
    }
}