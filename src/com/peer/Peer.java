package com.peer;
import com.handlers.CommandHandler;
import com.handlers.PackageHandler;
import com.protocols.DeleteFileProtocol;
import com.protocols.PutFileProtocol;
import com.protocols.RemoveChunkProtocol;
import com.protocols.RestoreFileProtocol;
import com.utils.*;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Represents a Server Peer. Contains main method
 */

public class Peer implements PeerInterface {


    private long totalFreeSpace; // Total space that the peer may use;
    private int serverID;
    /**
     * The multicast port of the corresponding socket
     */
    private int mcPort;
    private int mdbPort;
    private int mdrPort;

    /**
     * The multicast address of the corresponding socket
     */
    private InetAddress mcAddress;
    private InetAddress mdbAddress;
    private InetAddress mdrAddress;

    /**
     * The UDP multicast Sockets that are run on each peer
     */
    private MulticastSocket MC;
    private MulticastSocket MDB;
    private MulticastSocket MDR;

    private LinkedBlockingQueue<RestoreFileProtocol> myRestoreRequests; // A list of the files that the peer wants to restore

    /**
     * The main method. Starts the peer.
     * @param args the arguments to be passed by the user at the start of the application.
     */
    public static void main(String[] args) {

        if(args.length != 7){
            System.err.println("Usage: <serverID> <mcIP> <mcPort> <mdbIP> <mdbPort> <mdrIP> <mdrPort>");
            System.exit(-1);
        }

        int serverID = Integer.parseInt(args[0]);
        String mcInfo = args[1];
        String mdbInfo = args[3];
        String mdrInfo = args[5];
        int mcPort = Integer.parseInt(args[2]);
        int mdbPort = Integer.parseInt(args[4]);
        int mdrPort = Integer.parseInt(args[6]);
        InetAddress mcAddress = null;
        InetAddress mdbAddress = null;
        InetAddress mdrAddress = null;
        try {
            mcAddress = InetAddress.getByName(mcInfo);
            mdbAddress = InetAddress.getByName(mdbInfo);
            mdrAddress = InetAddress.getByName(mdrInfo);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        Peer testPeer = new Peer(serverID, mcPort, mdbPort, mdrPort, mcAddress, mdbAddress, mdrAddress);
        try {
            PeerInterface stub = (PeerInterface) UnicastRemoteObject.exportObject(testPeer, serverID);
            Registry registry = LocateRegistry.createRegistry(serverID);
            registry.rebind("Peer",stub);
        } catch (RemoteException e) {
            System.err.println("Cannot export RMI Object");
            System.exit(-1);
        }

        CommandHandler.getInstance(testPeer);
        testPeer.joinMulticastGroups();

        testPeer.startHandlers();
    }



    /**
     * Constructor for Peer. Instantiates the object and saves the ports and addresses
     * @param serverID the ID of the peer server
     * @param mcPort the port of the MC channel
     * @param mdbPort the port of the MDB channel
     * @param mdrPort the port of the MDR channel
     * @param mcAddress the address of the MC channel
     * @param mdbAddress the address of the MDB channel
     * @param mdrAddress the address of he MDR channel
     */
    public Peer(int serverID, int mcPort, int mdbPort,int mdrPort, InetAddress mcAddress, InetAddress mdbAddress, InetAddress mdrAddress){
        this.serverID = serverID;
        this.mcPort = mcPort;
        this.mdbPort = mdbPort;
        this.mdrPort = mdrPort;
        this.mcAddress = mcAddress;
        this.mdbAddress = mdbAddress;
        this.mdrAddress = mdrAddress;
        this.myRestoreRequests = new LinkedBlockingQueue<>();
        this.totalFreeSpace = Constants.PEER_TOTAL_SPACE;
    }

    /**
     * Open all sockets and have them join their respective multicast groups
     */
    private void joinMulticastGroups() {
        //open sockets
        try {
            MC = new MulticastSocket(mcPort);
            MDB = new MulticastSocket(mdbPort);
            MDR = new MulticastSocket(mdrPort);

            MC.setTimeToLive(1);
            MDB.setTimeToLive(1);
            MDR.setTimeToLive(1);

            MC.joinGroup(mcAddress);
            MDB.joinGroup(mdbAddress);
            MDR.joinGroup(mdrAddress);
        } catch (IOException e) {
            System.err.println("Cannot join to all multicast channels");
            System.exit(-1);
        }

        System.out.println("Succesfully joined groups");
    }

    /**
     * Start the singleton thread Command Handler. It takes care of processing any arriving commands so as to free
     * processing time on the multicast channel handlers
     * Start all channel handlers. They take care of receiving packets on each multicast channel and sending them to the Command Handler
     */
    public void startHandlers(){
        CommandHandler singletonHandler = CommandHandler.getInstance(this);
        singletonHandler.start();

        PackageHandler mcChannelHandler = new PackageHandler(MC, mcAddress, mcPort, "MC");
        mcChannelHandler.start();

        PackageHandler mdbChannelHandler = new PackageHandler(MDB, mdbAddress, mdbPort, "MDB");
        mdbChannelHandler.start();

        PackageHandler mdrChannelHandler = new PackageHandler(MDR, mdrAddress, mdrPort, "MDR");
        mdrChannelHandler.start();
    }

    /**
     * Get the MC channel socket
     * @return the MC channel socket
     */
    public MulticastSocket getMC() {
        return MC;
    }

    /**
     * Get the MDB channel socket
     * @return the MDB channel socket
     */
    public MulticastSocket getMDB() {
        return MDB;
    }

    /**
     * Get the MDR channel socket
     * @return the MDR channel socket
     */
    public MulticastSocket getMDR() {
        return MDR;
    }

    public int getServerID() {
        return serverID;
    }

    public int getMcPort() {
        return mcPort;
    }

    public int getMdbPort() {
        return mdbPort;
    }

    public int getMdrPort() {
        return mdrPort;
    }

    public InetAddress getMdbAddress() {
        return mdbAddress;
    }

    public InetAddress getMcAddress() {
        return mcAddress;
    }

    public InetAddress getMdrAddress() {
        return mdrAddress;
    }

    /**
     * Save a file on the service net
     * @param fileName name of the file to be saved
     * @param repDegree desired replication degree
     */
    public void putFile(String fileName, int repDegree) {
        System.out.println("Starting backup protocol for file " + fileName);
        PutFileProtocol chunkPutter = new PutFileProtocol(this, fileName, repDegree);
        chunkPutter.start();
    }

    public void restoreFile(String fileName){
        RestoreFileProtocol restore = new RestoreFileProtocol(this,fileName, MyFiles.getInstance().getFileId(fileName), MyFiles.getInstance().getNumberOfChunks(fileName));
        myRestoreRequests.add(restore);
        new Thread(restore).start();
    }

    public void receivedChunk(String fileId, int chunkNo, byte[] data) {
        for(RestoreFileProtocol fileProtocol : myRestoreRequests) {
            if(fileProtocol.getFileId().equals(fileId)) {
                fileProtocol.addBytes(chunkNo,data);
            }
        }
    }

    public void removeRestoreRequest(RestoreFileProtocol restoreFileProtocol) {
        myRestoreRequests.remove(restoreFileProtocol);
    }
    /**
     * Delete a file on the service net and locally
     * @param fileName the name of the file to be deleted
     */
    public void deleteFile(String fileName) {
        System.out.println("Starting file deletion protocol for file " + fileName);

        DeleteFileProtocol fileDeleter = new DeleteFileProtocol(this, fileName);
        fileDeleter.start();
    }

    /**
     * Get a local file. Makes sure it exists and is not a directory
     * @param fileName the name of the file
     * @return the file
     */
    public File getLocalFile(String fileName) {
        //Check if file exists
        File f = new File(Constants.FILE_PATH + fileName);
        if (!f.exists() || f.isDirectory()) {
            System.err.println("Please make sure a file exists before accessing it.");
            return null;
        }

        return f;
    }

    public void reclaimSpace(int totalSpace){
        if(this.totalFreeSpace >= totalSpace){
            this.totalFreeSpace -= totalSpace;
        }
        else{
            this.totalFreeSpace = 0;
        }
        long total = getTotalChunksSize();
        System.out.println("NEW TOTAL SPACE: " + this.totalFreeSpace);
        System.out.println("CURRENT CHUNKS TOTAL SIZE: " +  total);
        if(this.totalFreeSpace < total){
            File server = new File(Constants.FILE_PATH + serverID);
            File[] dirs = server.listFiles();
            for (File dir:dirs) {
                if(dir.isDirectory() && !dir.getName().equals("restored")){
                    File[] files = dir.listFiles();
                    for (File f: files) {
                        String filename = f.getName();
                        int pos = filename.lastIndexOf(".");
                        int chunkNo = Integer.parseInt(filename.substring(0, pos));
                        ReplicationInfo rep = ChunksInfo.getInstance().getInfo(dir.getName(),chunkNo);
                        synchronized(rep){
                            if(rep != null){
                                if(rep.getActualRepDegree() >= rep.getActualRepDegree()){
                                    new Thread(new RemoveChunkProtocol(dir.getName(),chunkNo, this)).start();
                                    if (this.totalFreeSpace >= getTotalChunksSize()) {
                                        return;
                                    }
                                    f.delete();
                                }
                            }
                        }
                    }
                    files = dir.listFiles();
                    if (this.totalFreeSpace < getTotalChunksSize()) {
                        for (File f: files) {
                            String filename = f.getName();
                            int pos = filename.lastIndexOf(".");
                            int chunkNo = Integer.parseInt(filename.substring(0, pos));
                            new Thread(new RemoveChunkProtocol(dir.getName(),chunkNo, this)).start();
                            if (this.totalFreeSpace >= getTotalChunksSize()) {
                                return;
                            }
                            f.delete();
                        }
                    }
                }
            }

        }

    }

    public long getTotalChunksSize(){
        long totalSize = 0;
        File serverDir = new File(Constants.FILE_PATH + serverID);
        File[] dirs = serverDir.listFiles();
        if(dirs != null){
            for (File dir : dirs) {
                if(dir.isDirectory() && !dir.getName().equals("restored")){
                    File[] files = dir.listFiles();
                    if(files != null){
                        for (File f: files) {
                            totalSize += f.length();
                        }
                    }
                }
            }
        }

        return totalSize;
    }

    public long getTotalFreeSpace() {
        return totalFreeSpace;
    }
}

