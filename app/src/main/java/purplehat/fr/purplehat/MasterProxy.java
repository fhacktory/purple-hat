package purplehat.fr.purplehat;

/**
 * Created by turpif on 11/10/14.
 */
public class MasterProxy {
    private final static Master instance = new Master(1618, "4242424242424242424242", null);

    public synchronized static Master getMaster() {
        return instance;
    }
}
