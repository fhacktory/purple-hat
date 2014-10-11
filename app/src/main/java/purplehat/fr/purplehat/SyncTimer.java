package purplehat.fr.purplehat;

/**
 * Created by vcaen on 11/10/2014.
 */
public class SyncTimer extends Thread {

    long relativeTime = 0;
    long syncTime;
    boolean running;
    final Object lock = new Object();

    public void startAt(long time) {
        syncTime = time;
        start();
    }

    public long getRelativeTime() {
        synchronized (lock) {
            return relativeTime;
        }
    }


    @Override
    public void run() {
        try {
            Thread.sleep(syncTime - System.currentTimeMillis());
            while(true) {
                Thread.sleep(1);
                synchronized (lock) {
                    relativeTime++;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
