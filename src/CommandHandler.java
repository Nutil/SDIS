import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

//import apache FileUtils


/**
 * Singleton Class that handles and dispatches all peer commands
 */
public class CommandHandler extends Thread {
    private static CommandHandler commandHandler = null;
    private LinkedList<byte[]> commands;
    private LinkedBlockingQueue<String> restoreRequests;
    private static Peer peer;

    private CommandHandler(Peer peer){
        this.peer = peer;
        commands = new LinkedList<>();
        restoreRequests = new LinkedBlockingQueue<>();
    }

    public static CommandHandler getInstance(Peer peer){
        if(commandHandler == null){
            commandHandler = new CommandHandler(peer);
        }
        return commandHandler;
    }
    public static CommandHandler getInstance(){
        return commandHandler;
    }

    public void run(){
        while(true){
            //Sleep thread if no commands to be handled
            if(commands.size() == 0){
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Queue isn't empty, handle that command
            System.out.println("Working on queued commands. Commands queue size: " + commands.size());
            byte[] command = commands.poll();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String handledCommand = handleCommand(command);
                }
            }).start();
        }
    }

    private String handleCommand(byte[] commandPacket){
        Message msg = new Message(commandPacket);
        String requestName = msg.getHeader().getFileId() + "_" + msg.getHeader().getChunkNo();
        System.out.println(msg.getHeader().getMessageType());
        switch (msg.getHeader().getMessageType()){
            case "PUTCHUNK":
                if(!msg.getHeader().getVersion().equals(Constants.PROTOCOL_VERSION))
                    break;
                if(msg.getHeader().getSenderId() == peer.getServerID())
                    break;
                handlePutchunk(msg);
                break;
            case "STORED":
                if(!msg.getHeader().getVersion().equals(Constants.PROTOCOL_VERSION))
                    break;
                handleStored(msg);
                break;
            case "GETCHUNK":
                if(msg.getHeader().getSenderId() == peer.getServerID())
                    break;
                if(!msg.getHeader().getVersion().equals(Constants.PROTOCOL_VERSION))
                    break;
                handleRestore(msg);
                break;
            case "CHUNK":
                restoreRequests.remove(requestName);
                peer.receivedChunk(msg.getHeader().getFileId(),msg.getHeader().getChunkNo(), msg.getBody());
                break;
            case "DELETE":
                if(msg.getHeader().getSenderId() == peer.getServerID())
                    break;

                handleDelete(msg);
                break;
            case "REMOVED":
                if(msg.getHeader().getSenderId() == peer.getServerID())
                    break;

                handleRemove(msg);
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
    public void addCommand(byte[] command) {
        commands.add(command);
    }

    /**
     * Function to handle the Putchunk message
     * @param msg the Putchunk message
     */
    private void handlePutchunk(Message msg){
        File chunk;
        File serverDir = new File(Constants.FILE_PATH + peer.getServerID());
        File chunkDir = new File(serverDir, msg.getHeader().getFileId());
        if(!chunkDir.exists()){
            chunkDir.mkdirs();
        }
        chunk = new File(chunkDir,msg.getHeader().getChunkNo() + Constants.FILE_EXTENSION);
        try {
            System.out.println("Creating new chunk file and writing chunk.");
            chunk.createNewFile();
            FileOutputStream out = new FileOutputStream(chunk);
            out.write(msg.getBody());
            out.close();
            System.out.println("Finished storing chunk file 837645872365473456234"); // WTF???
            Header rspHeader = new Header("STORED", Constants.PROTOCOL_VERSION, peer.getServerID(),
                    msg.getHeader().getFileId(), msg.getHeader().getChunkNo(), Constants.REP_DEGREE_IGNORE);
            Message rsp = new Message(rspHeader,null);
            MulticastSocket socket = peer.getMC();
            DatagramPacket packet = new DatagramPacket(rsp.getBytes(), rsp.getBytes().length, peer.getMcAddress(), peer.getMcPort());
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

    /**
     * Function to handle the Stored message
     * @param msg the Stored message
     */
    public void handleStored(Message msg){
        int actualRepDeg = 0;
        ReplicationInfo repInfo = FileInfo.getInstance().getInfo(msg.getHeader().getFileId(),msg.getHeader().getChunkNo());
        if(repInfo != null){
            actualRepDeg = repInfo.getActualRepDegree();
        }
        FileInfo.getInstance().addInfo(msg.getHeader().getFileId(),msg.getHeader().getChunkNo(),actualRepDeg +1,msg.getHeader().getReplicationDegree());
    }

    /**
     * Function to handle the Restore message
     * @param msg the Restore message
     */
    public void handleRestore(Message msg){

        String requestName = msg.getHeader().getFileId() + "_" + msg.getHeader().getChunkNo();
        restoreRequests.add(requestName);
        File dir = new File(Constants.FILE_PATH + peer.getServerID(), msg.getHeader().getFileId());
        File chunk = new File(dir, msg.getHeader().getChunkNo() + Constants.FILE_EXTENSION);
        if(chunk.exists() && !chunk.isDirectory()){
            Random rn = new Random();
            int randomDelay = rn.nextInt(Constants.delay + 1);
            byte[] chunkData = new byte[Constants.chunkSize];
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chunk));
                int bytesRead = bis.read(chunkData);
                chunkData = Arrays.copyOf(chunkData,bytesRead);
                Header rpsHeader = new Header("CHUNK",Constants.PROTOCOL_VERSION,peer.getServerID(), msg.getHeader().getFileId(),msg.getHeader().getChunkNo(),-1);
                Message rsp = new Message(rpsHeader,chunkData);
                Thread.sleep(randomDelay);
                if(restoreRequests.remove(requestName)){
                    DatagramPacket chunkPacket = new DatagramPacket(rsp.getBytes(),rsp.getBytes().length, peer.getMdrAddress(), peer.getMdrPort());
                    peer.getMDR().send(chunkPacket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        restoreRequests.add(requestName);
    }

    /**
     * Function to handle the Delete message
     * @param msg the Delete message
     */
    public void handleDelete(Message msg){
        System.out.println("Received delete command. Deleting local table entries...");
        FileInfo theInfo = FileInfo.getInstance();
        theInfo.removeFileEntries(msg.getHeader().getFileId());

        File f = new File(Constants.FILE_PATH + peer.getServerID(),msg.getHeader().getFileId());
        if(!f.exists())
            return;
        if(!f.isDirectory()){
            System.err.println("Error: file exists but is not a directory");
            return;
        }

        File[] dirFiles = f.listFiles();
        for (File file : dirFiles) {
            file.delete();
        }
        f.delete();

    }

    public void handleRemove(Message msg){
        File chunk = new File(Constants.FILE_PATH + peer.getServerID() + File.separator + msg.getHeader().getFileId()
                , "" + msg.getHeader().getChunkNo());
        if(!chunk.exists()){
            return;
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(chunk));
            byte [] chunkData = new byte[Constants.chunkSize];
            int timeToSleep = 100;
            int chunkRepDegree = FileInfo.getInstance().getInfo(msg.getHeader().getFileId(),msg.getHeader().getChunkNo()).getActualRepDegree();
            int bytesRead = bis.read(chunkData);
            chunkData = Arrays.copyOf(chunkData, bytesRead);

            Random rn = new Random();
            int randomDelay = rn.nextInt(Constants.delay + 1);
            Thread.sleep(randomDelay);

            ReplicationInfo chunkInfo = FileInfo.getInstance().getInfo(msg.getHeader().getFileId(),msg.getHeader().getChunkNo());

            if(chunkInfo.getActualRepDegree() < chunkInfo.getDesiredRepDegree()){
                for(int resends = 0; resends < 5 && chunkRepDegree < chunkInfo.getDesiredRepDegree(); resends++) {
                    chunkRepDegree = FileInfo.getInstance().getInfo(msg.getHeader().getFileId(),msg.getHeader().getChunkNo()).getActualRepDegree();
                    Header messageHeader = new Header("PUTCHUNK", Constants.PROTOCOL_VERSION, peer.getServerID(), msg.getHeader().getFileId(), msg.getHeader().getChunkNo(), chunkInfo.getDesiredRepDegree());
                    Message response = new Message(messageHeader, chunkData);
                    DatagramPacket requestPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, peer.getMdbAddress(), peer.getMdbPort());
                    peer.getMDB().send(requestPacket);

                    //Await peer responses
                    Thread.sleep(timeToSleep *(long) Math.pow(1, (double)resends));
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}