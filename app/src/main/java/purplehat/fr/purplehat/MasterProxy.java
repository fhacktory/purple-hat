package purplehat.fr.purplehat;

/**
 * Created by turpif on 11/10/14.
 */
public class MasterProxy {
    public final static int MASTER_PROXY_PORT_DE_OUF = 1618;
    private final static Master instance = new Master(MASTER_PROXY_PORT_DE_OUF, "4242424242424242424242", null);

    public synchronized static Master getMaster() {
        return instance;
    }
}
