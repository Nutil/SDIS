package com.utils;

import com.handlers.CommandHandler;

import java.io.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Diogo Guarda on 02/04/2016.
 */
public class MyFiles implements Serializable{

    private static MyFiles filesInfo = null;
    private ConcurrentHashMap<String, BasicFileAttributes> metadata;

    private MyFiles() {
        this.metadata = new ConcurrentHashMap<>();
    }

    public static MyFiles getInstance(){
        if(filesInfo == null){
            if(!loadClass()){
                filesInfo = new MyFiles();
            }
        }
        return filesInfo;
    }


    public void addFileInfo(String filename,BasicFileAttributes attr){
        metadata.put(filename,attr);
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveClass();
            }
        }).start();
    }

    private static boolean loadClass(){
        try
        {
            FileInputStream fileIn = new FileInputStream(Constants.FILE_PATH + CommandHandler.getPeer().getServerID() + File.separator + "myFiles.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            filesInfo = (MyFiles) in.readObject();
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
            File file = new File(dir, "myFiles.dat");
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
            return;
        }
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
