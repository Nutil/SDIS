import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Diogo Guarda on 02/04/2016.
 */
public class MyFiles {

    private static MyFiles filesInfo = null;
    private ConcurrentHashMap<String, BasicFileAttributes> metadata;

    private MyFiles() {
        this.metadata = new ConcurrentHashMap<>();
    }

    public static MyFiles getInstance(){
        if(filesInfo == null){
            filesInfo = new MyFiles();
        }
        return filesInfo;
    }

    public void addFileInfo(String filename,BasicFileAttributes attr){
        metadata.put(filename,attr);
    }

    public String getFileId(String filename){
        BasicFileAttributes attr = metadata.get(filename);
        return Constants.sha256(filename + " " + attr.creationTime() + " " + attr.lastModifiedTime() + " " + attr.size());
    }

    public int getNumberOfChunks(String filename){
        long fileSize = metadata.get(filename).size();
        double chunks = Math.ceil((double)fileSize/(double)Constants.chunkSize);
        return (int)chunks;
    }

    public void removeInfo(String fileName) {
        metadata.remove(fileName);
    }
}
