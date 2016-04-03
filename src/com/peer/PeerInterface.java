package com.peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Diogo Guarda on 31/03/2016.
 */
public interface PeerInterface extends Remote {
    void putFile(String filename, int repDegree) throws RemoteException;
    void restoreFile(String filename) throws RemoteException;
    void deleteFile(String filename) throws RemoteException;
    void reclaimSpace(int space) throws RemoteException;
}
