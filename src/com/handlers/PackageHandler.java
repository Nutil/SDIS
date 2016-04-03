package com.handlers;

import com.utils.Constants;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

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
                mcSocket.receive(receptionPacket);
                byte[] dataRead = Arrays.copyOf(receptionPacket.getData(),receptionPacket.getLength());
                commandHandler.addCommand(dataRead);
            } catch (Exception e) {
                System.err.println("Error: Message dropped");
            }
        }
    }
}
