package com.utils;

import java.io.Serializable;

/**
 * Created by Diogo Guarda on 25/03/2016.
 */
public class ReplicationInfo implements Serializable{
    private int desiredRepDegree;
    private int actualRepDegree;

    public ReplicationInfo(int desiredRepDegree, int actualRepDegree) {
        this.desiredRepDegree = desiredRepDegree;
        this.actualRepDegree = actualRepDegree;
    }

    public synchronized int getDesiredRepDegree() {
        return desiredRepDegree;
    }

    public synchronized int getActualRepDegree() {
        return actualRepDegree;
    }
    public synchronized void setActualRepDegree(int actualRepDegree) {
        this.actualRepDegree = actualRepDegree;
    }

}
