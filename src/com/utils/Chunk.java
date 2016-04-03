package com.utils;

import java.io.Serializable;

/**
 * Created by Diogo Guarda on 03/04/2016.
 */
public class Chunk implements Serializable{
    public Chunk(int chunkNo, String fileId) {
        this.chunkNo = chunkNo;
        this.fileId = fileId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chunk chunk = (Chunk) o;

        if (getChunkNo() != chunk.getChunkNo()) return false;
        return getFileId().equals(chunk.getFileId());

    }

    @Override
    public int hashCode() {
        int result = getChunkNo();
        result = 31 * result + getFileId().hashCode();
        return result;
    }

    private int chunkNo;
    private String fileId;

}
