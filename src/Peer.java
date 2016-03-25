import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.IOException;
import java.net.*;


/**
 * Represents a Server Peer. Contains main method
 */
public class Peer {

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

        Peer testPeer = new Peer(mcPort, mdbPort, mdrPort, mcAddress, mdbAddress, mdrAddress);

        testPeer.joinMulticastGroups();

        testPeer.startHandlers();

        //Test handler
        testPeer.sendMCRequest();

    }

    /**
     * Constructor for Peer. Instantiates the object and saves the ports and addresses
     * @param mcPort the port of the MC channel
     * @param mdbPort the port of the MDB channel
     * @param mdrPort the port of the MDR channel
     * @param mcAddress the address of the MC channel
     * @param mdbAddress the address of the MDB channel
     * @param mdrAddress the address of he MDR channel
     */
    public Peer(int mcPort, int mdbPort,int mdrPort, InetAddress mcAddress, InetAddress mdbAddress, InetAddress mdrAddress){
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

    /**
     * Temporary test method to simulate a multicast request.
     */
    public void sendMCRequest(){
        String message = "STORED 1.0";
        DatagramPacket requestPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, mcAddress, mcPort);
        try {
            System.out.println("Sending request package");
            MC.send(requestPacket);
            System.out.println("Package successfully sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

