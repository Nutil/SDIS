import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.*;


/**
 * Represents a Server Peer. Contains main method
 */

public class Peer {

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

    /**
     * The main method. Starts the peer.
     * @param args the arguments to be passed by the user at the start of the application.
     */
    public static void main(String[] args) {

        int serverID = Integer.parseInt(args[0]);
        int mcPort = 9000;
        int mdbPort = 9001;
        int mdrPort = 9002;
        InetAddress mcAddress = null;
        InetAddress mdbAddress = null;
        InetAddress mdrAddress = null;
        try {
            mcAddress = InetAddress.getByName("225.0.0.1");
            mdbAddress = InetAddress.getByName("225.0.0.2");
            mdrAddress = InetAddress.getByName("225.0.0.3");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Peer testPeer = new Peer(serverID, mcPort, mdbPort, mdrPort, mcAddress, mdbAddress, mdrAddress);

        testPeer.joinMulticastGroups();

        testPeer.startHandlers();

        //Test handler
        testPeer.putFile("teste1.txt", 1);

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
        } catch (IOException e) {
            e.printStackTrace();
        }

        //join groups
        try {
            MC.joinGroup(mcAddress);
            MDB.joinGroup(mdbAddress);
            MDR.joinGroup(mdrAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Succesfully joined groups");
    }

    /**
     * Start the singleton thread Command Handler. It takes care of processing any arriving commands so as to free
     * processing time on the multicast channel handlers
     * Start all channel handlers. They take care of receiving packets on each multicast channel and sending them to the Command Handler
     */
    public void startHandlers(){
        CommandHandler singletonHandler = CommandHandler.getInstance();
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

        PutchunkProtocol chunkPutter = new PutchunkProtocol(this, fileName, repDegree);
        chunkPutter.start();
    }

    public void restoreFile(String filePath){
        Constants.sha256(filePath);
        File f = new File(filePath);
    }
    /**
     * Sends to the MC channel GETCHUNK message
     * @param fileId specifies the id of the file
     * @param chunkNo specifies the number of the chunk being retrieved
     */
    public void getChunk(String fileId, int chunkNo){
        Header header = new Header("GETCHUNK","1.0",this.serverID,fileId,chunkNo,-1);
        Message message = new Message(header, null);
        DatagramPacket requestPacket = new DatagramPacket(message.getBytes(),message.getBytes().length, mcAddress, mcPort);
        try {
            System.out.println("Sending getchunk message");
            MC.send(requestPacket);
            System.out.println("Getchunk successfully sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

