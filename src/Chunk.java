/**
 * Created by Luís on 16/03/2016.
 */
public class Chunk {
    private int chunkIndex;
    private String fileHash;

    public Chunk (int chunkIndex, String fileHash) {
        this.chunkIndex = chunkIndex;
        this.fileHash = fileHash;
    }


}
