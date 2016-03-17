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

    public void startHandlers(){
        MCHandler mcChannelHandler = new MCHandler(MC, mcAddress, mcPort);
        mcChannelHandler.start();
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
        String message = "My Request";
        DatagramPacket requestPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, mcAddress, mcPort);
        try {
            System.out.println("Sending request package");
            MC.send(requestPacket);
            System.out.println("Package successfulyl sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

