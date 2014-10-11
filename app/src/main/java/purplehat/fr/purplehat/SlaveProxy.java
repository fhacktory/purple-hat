package purplehat.fr.purplehat;

/**
 * Created by turpif on 11/10/14.
 */
public class SlaveProxy {
    private final static Slave instance = new Slave();

    public synchronized static Slave getSlave() {
        return instance;
    }
}
