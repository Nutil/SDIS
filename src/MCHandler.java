import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;

/**
 * Created by Luís on 17/03/2016.
 */
public class MCHandler extends PackageHandler{

    public MCHandler(MulticastSocket mcSocket, InetAddress mcAddress, int mcPort){
        super(mcSocket, mcAddress, mcPort);
    }

    public void run() {
        byte[] packetData = new byte[65536];
        DatagramPacket receptionPacket = new DatagramPacket(packetData, 65536, mcAddress, mcPort);
        String receivedData = null;
        while(true) {
            try {
                mcSocket.receive(receptionPacket);
                receivedData = new String(receptionPacket.getData(), 0, receptionPacket.getLength());
                System.out.println("Received: " + receivedData);
                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
