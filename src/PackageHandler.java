import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Luís on 17/03/2016.
 */
public class PackageHandler extends Thread {
    private MulticastSocket mcSocket;
    private InetAddress mcAddress;
    private int mcPort;
    private String name;

    public PackageHandler(MulticastSocket mcSocket, InetAddress mcAddress, int mcPort, String name){
        this.mcSocket = mcSocket;
        this.mcAddress = mcAddress;
        this.mcPort = mcPort;
        this.name = name;
    }

    /**
     * Abstract method that contains the core functionality of the package handler, namely, running the thread that
     */
    public void run() {
        byte[] packetData = new byte[65536];
        DatagramPacket receptionPacket = null;
        String receivedData = null;
        CommandHandler commandHandler = CommandHandler.getInstance();

        System.out.println("Listening on multicast group " + name);
        while(true) {
            try {
                receptionPacket = new DatagramPacket(packetData, 65536, mcAddress, mcPort);
                System.out.println("Awaiting packets on " + name);
                mcSocket.receive(receptionPacket);
                System.out.println("Received a packet on " + name + ". Calling command handler");
                commandHandler.addCommand(receptionPacket);
                System.out.println("Added packet to command handler. Continuing...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
