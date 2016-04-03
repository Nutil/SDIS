import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by Diogo Guarda on 25/03/2016.
 */
public class ChunksInfo implements Serializable{
    private static ChunksInfo info = null;
    private Hashtable<String,ReplicationInfo> filesInfo;

    private ChunksInfo(){
        filesInfo = new Hashtable<String,ReplicationInfo>();
    }

    public static ChunksInfo getInstance(){
        if(info == null){
            if(!loadClass()){
                info = new ChunksInfo();
            }
        }
        return info;
    }
    private static boolean loadClass(){
        try
        {
            FileInputStream fileIn = new FileInputStream(Constants.FILE_PATH + CommandHandler.getPeer().getServerID() + File.separator + "chunksInfo.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            info = (ChunksInfo) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i)
        {
            return false;
        }catch(ClassNotFoundException c)
        {
            return false;
        }
        return true;
    }

    private void saveClass() {
        try
        {
            FileOutputStream fileOut = new FileOutputStream(Constants.FILE_PATH + CommandHandler.getPeer().getServerID() + File.separator + "chunksInfo.dat");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void addInfo(String fileId, int chunkNo, int actualRepDegree, int desiredRepDegree){
        ReplicationInfo info = new ReplicationInfo(desiredRepDegree, actualRepDegree);
        if(filesInfo.get(fileId + "_" + chunkNo) == null) {
            filesInfo.put(fileId + "_" + chunkNo, info);
        }
        else{
            filesInfo.replace(fileId + "_" + chunkNo,info);
        }
        saveClass();
    }

    public ReplicationInfo getInfo(String fileId, int chunkNo){
        return filesInfo.get(fileId + "_" + chunkNo);
    }

    /**
     * Removes all table chunks belonging to the specified file from the table
     * @param fileID the fileID of the entries to be deleted
     */
    public void removeFileEntries(String fileID){
        Iterator<String> it = filesInfo.keySet().iterator();

        String chunkKey;
        while(it.hasNext()){
            chunkKey = it.next();

            //Didn't match
            if(!chunkKey.contains(fileID))
                continue;

            it.remove();
        }
        saveClass();
    }

    public void updateInfo(String fileId, int chunkNo) {
        ReplicationInfo info = filesInfo.get(fileId+"_"+chunkNo);
        info.setActualRepDegree(info.getActualRepDegree() - 1);
        saveClass();
    }
}
