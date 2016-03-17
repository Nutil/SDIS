import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Luís on 16/03/2016.
 */

import java.io.IOException;
import java.net.*;
public class Peer {

    private int mcPort;
    private int mdbPort;
    private int mdrPort;
    private InetAddress mcAddress;
    private InetAddress mdbAddress;
    private InetAddress mdrAddress;
    private MulticastSocket MC;
    private MulticastSocket MDB;
    private MulticastSocket MDR;

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
     * Start all channel handlers. They take care of receiving and processing packets on each channel
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

    public MulticastSocket getMC() {
        return MC;
    }

    public MulticastSocket getMDB() {
        return MDB;
    }

    public MulticastSocket getMDR() {
        return MDR;
    }

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

