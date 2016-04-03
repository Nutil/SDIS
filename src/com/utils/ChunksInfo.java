package com.utils;

import com.handlers.CommandHandler;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Diogo Guarda on 25/03/2016.
 */
public class ChunksInfo implements Serializable{
    private static ChunksInfo info = null;
    private ConcurrentHashMap<Chunk, ReplicationInfo> filesInfo;

    private ChunksInfo(){
        filesInfo = new ConcurrentHashMap<>();
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
            File dir = new File(Constants.FILE_PATH + CommandHandler.getPeer().getServerID());
            dir.mkdir();
            File file = new File(dir,"chunksInfo.dat");
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        }catch(IOException e)
        {

        }
    }

    public void addInfo(String fileId, int chunkNo, int actualRepDegree, int desiredRepDegree){
        synchronized (filesInfo){
            ReplicationInfo info = new ReplicationInfo(desiredRepDegree, actualRepDegree);
            filesInfo.putIfAbsent(new Chunk(chunkNo,fileId), info);
        }
        saveClass();
    }

    public ReplicationInfo getInfo(String fileId, int chunkNo){
        synchronized (filesInfo) {
            return filesInfo.get(new Chunk(chunkNo,fileId));
        }
    }

    /**
     * Removes all table chunks belonging to the specified file from the table
     * @param fileID the fileID of the entries to be deleted
     */
    public void removeFileEntries(String fileID){
        synchronized (filesInfo) {
            Iterator<Chunk> it = filesInfo.keySet().iterator();

            Chunk chunkKey;
            while (it.hasNext()) {
                chunkKey = it.next();

                //Didn't match
                if (!chunkKey.getFileId().equals(fileID))
                    continue;

                it.remove();
            }
        }
        saveClass();
    }

    public void updateInfo(String fileId, int chunkNo) {
        synchronized (filesInfo) {
            ReplicationInfo info = filesInfo.get(new Chunk(chunkNo,fileId));
            info.setActualRepDegree(info.getActualRepDegree() - 1);
        }
        saveClass();
    }

    public synchronized void addActualDegree(String fileId, int chunkNo) {
        synchronized (filesInfo) {
            ReplicationInfo info = filesInfo.get(new Chunk(chunkNo,fileId));
            info.setActualRepDegree(info.getActualRepDegree() + 1);
        }
        saveClass();
    }

    public ConcurrentHashMap<Chunk, ReplicationInfo> getFilesInfo() {
        return filesInfo;
    }
}
