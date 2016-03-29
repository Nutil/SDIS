import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Diogo Guarda on 25/03/2016.
 */
public class FileInfo {
    private static FileInfo info = null;
    private Hashtable<String,ReplicationInfo> filesInfo;

    private FileInfo(){
        filesInfo = new Hashtable<String,ReplicationInfo>();
    }

    public static FileInfo getInstance(){
        if(info == null){
            info = new FileInfo();
        }
        return info;
    }

    public void addInfo(String fileId, int chunkNo, int actualRepDegree, int desiredRepDegree){
        ReplicationInfo info = new ReplicationInfo(desiredRepDegree, actualRepDegree);
        if(filesInfo.get(fileId + "_" + chunkNo) == null) {
            filesInfo.put(fileId + "_" + chunkNo, info);
        }
        else{
            filesInfo.replace(fileId + "_" + chunkNo,info);
        }
        //TO-DO Save info in non-volatile memory
    }

    public ReplicationInfo getInfo(String fileId, int chunkNo){
        return filesInfo.get(fileId + "_" + chunkNo);
    }

}
