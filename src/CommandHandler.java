import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.Random;


/**
 * Singleton Class that handles and dispatches all peer commands
 */
public class CommandHandler extends Thread {
    private static CommandHandler commandHandler = null;
    private static LinkedList<DatagramPacket> commands;
    private static Peer peer;

    private CommandHandler(Peer peer){
        this.peer = peer;
        commands = new LinkedList<DatagramPacket>();
    }

    public static CommandHandler getInstance(Peer peer){
        if(commandHandler == null){
            commandHandler = new CommandHandler(peer);
        }
        return commandHandler;
    }

    public void run(){
        while(true){
            System.out.println("Checking queued commands...");
            //Sleep thread if no commands to be handled
            if(commands.size() == 0){
                System.out.println("No commands found. Sleeping...");
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Queue isn't empty, handle that command
            System.out.println("Working on queued commands. Commands queue size: " + commands.size());
            DatagramPacket command = commands.poll();
            String handledCommand = handleCommand(command);
            System.out.println(handledCommand + " | Command handling finished.");
            System.out.println("Commands queue size: " + commands.size() + ". Continuing...");
        }
    }

    private String handleCommand(DatagramPacket commandPacket){
        Message msg = new Message(commandPacket.getData());
        switch (msg.getHeader().getMessageType()){
            case "PUTCHUNK":
                if(msg.getHeader().getVersion().equals(Constants.PROTOCOL_VERSION)){
                    File chunkDir = new File(Constants.FILE_PATH + msg.getHeader().getFileId());
                    File chunk = new File(chunkDir,msg.getHeader().getChunkNo() + ".chunk");
                    try {
                        FileOutputStream out = new FileOutputStream(chunk);
                        out.write(msg.getBody());
                        out.close();
                        Header rspHeader = new Header("STORED", Constants.PROTOCOL_VERSION, peer.getServerID(),
                                msg.getHeader().getFileId(), msg.getHeader().getChunkNo(), -1);
                        Message rsp = new Message(rspHeader,null);
                        MulticastSocket socket = peer.getMC();
                        DatagramPacket packet = new DatagramPacket(rsp.getBytes(), rsp.getBytes().length, socket.getInetAddress(),socket.getPort());
                        Random rn = new Random();
                        int randomDelay = rn.nextInt(Constants.delay + 1);
                        Thread.sleep(randomDelay);
                        socket.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "STORED":
                
                break;
            default:
                System.out.println("Unrecognized command. Disregarding");
                break;
        }

        return msg.getHeader().getMessageType();
    }

    /**
     * Adds a command packet to the priority queue
     */
    public static void addCommand(DatagramPacket command) {
        commands.add(command);
        return;
    }

}
