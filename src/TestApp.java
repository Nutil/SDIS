import java.math.BigDecimal;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by Diogo Guarda on 28/03/2016.
 */
public class TestApp {

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.err.println("Usage: java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
            System.exit(-1);
        }

        String port = args[0];
        String protocol = args[1];
        String filename = args[2];
        try {
            Registry registry = LocateRegistry.getRegistry(Integer.parseInt(port));
            PeerInterface peer = (PeerInterface) registry.lookup("Peer");
            switch (protocol) {
                case "BACKUP":
                    int repDegree = 0;
                    if (args.length == 4) {
                        repDegree = Integer.parseInt(args[3]);
                    } else {
                        return;
                    }
                    peer.putFile(filename, repDegree);
                    break;
                case "RESTORE":
                    peer.restoreFile(filename);
                    break;
                case "DELETE":
                    peer.deleteFile(filename);
                    break;
                case "RECLAIM":
                    int space = Integer.parseInt(filename);
                    peer.reclaimSpace(space);
                    break;
                default:
                    System.err.println("Unkown subprotocol!");
            }
        } catch (AccessException e1) {
            e1.printStackTrace();
        } catch (RemoteException e1) {
            System.err.println("Could not access to RMI register");
        } catch (NotBoundException e1) {
            System.err.println("Impossible to acess peer!");
        }
    }

}
