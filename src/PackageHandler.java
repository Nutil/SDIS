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
        CommandHandler commandHandler = CommandHandler.getInstance();

        System.out.println("Listening on multicast group " + name);
        while(true) {
            try {
                byte[] packetData = new byte[Constants.PACKET_BUFFER_SIZE];
                DatagramPacket receptionPacket = new DatagramPacket(packetData, packetData.length);
                System.out.println("Awaiting packets on " + name);
                mcSocket.receive(receptionPacket);
                System.out.println("Received a packet on " + name + ". Calling command handler");
                commandHandler.addCommand(Constants.trim(receptionPacket.getData()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
