import java.net.DatagramPacket;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Singleton Class that handles and dispatches all peer commands
 */
public class CommandHandler extends Thread {
    private static CommandHandler commandHandler = new CommandHandler();
    private static LinkedList<DatagramPacket> commands;

    private CommandHandler(){
        commands = new LinkedList<DatagramPacket>();
    }

    public static CommandHandler getInstance(){ return commandHandler;}

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
        String packetData = new String(commandPacket.getData(), 0, commandPacket.getLength());
        String[] splitData = packetData.split(" ");
        switch (splitData[0]){
            case "STORED":
                System.out.println("Handling stored command");
                break;
            default:
                System.out.println("Unrecognized command. Disregarding");
                break;
        }

        return splitData[0];
    }

    /**
     * Adds a command packet to the priority queue
     */
    public static void addCommand(DatagramPacket command) {
        commands.add(command);
        return;
    }

}
