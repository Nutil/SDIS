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

    public int getDesiredRepDegree() {
        return desiredRepDegree;
    }

    public int getActualRepDegree() {
        return actualRepDegree;
    }
    public void setActualRepDegree(int actualRepDegree) {
        this.actualRepDegree = actualRepDegree;
    }

}
