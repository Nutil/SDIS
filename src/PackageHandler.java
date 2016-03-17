import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Luís on 17/03/2016.
 */
public abstract class PackageHandler extends Thread {
    protected MulticastSocket mcSocket;
    protected InetAddress mcAddress;
    protected int mcPort;

    public PackageHandler(MulticastSocket mcSocket, InetAddress mcAddress, int mcPort){
        this.mcSocket = mcSocket;
        this.mcAddress = mcAddress;
        this.mcPort = mcPort;
    }

    /**
     * Abstract method that contains the core functionality of the package handler, namely, running the thread that
     */
    public abstract void run();
}
