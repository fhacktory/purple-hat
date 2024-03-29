package purplehat.fr.purplehat.network;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import purplehat.fr.purplehat.FullscreenActivity;
import purplehat.fr.purplehat.Master;
import purplehat.fr.purplehat.PhysicalScreen;

/**
 * Created by turpif on 11/10/14.
 */
public class ConnectionListener implements Runnable {
    public final static int NEW_CONNEXION_PORT = 2048;
    private static final String LOG_TAG = "CONNECTION_LISTENER";

    private static class ActualConnectionListener implements Runnable {
        private final Socket socket;

        public ActualConnectionListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            byte[] newDeviceIdentifier = new byte[6];
            byte[] oldDeviceIdentifier = new byte[6];
            byte[] bdx = new byte[2];
            byte[] bdy = new byte[2];
            byte[] bwidth = new byte[2];
            byte[] bheight = new byte[2];
            byte[] bdirection = new byte[1];

            try {
                socket.getInputStream().read(newDeviceIdentifier, 0, 6);
                socket.getInputStream().read(oldDeviceIdentifier, 0, 6);
                socket.getInputStream().read(bdx, 0, 2);
                socket.getInputStream().read(bdy, 0, 2);
                socket.getInputStream().read(bwidth, 0, 2);
                socket.getInputStream().read(bheight, 0, 2);
                socket.getInputStream().read(bdirection, 0, 1);
            } catch (IOException e) {
                return;
            }

            String strNewDeviceIdentifier = new String(newDeviceIdentifier);
            String strOldDeviceIdentifier = new String(oldDeviceIdentifier);
            short dx = (short) ((bdx[0] & 0xff) + (short) ((bdx[1] & 0xff) << 8));
            short dy = (short) ((bdy[0] & 0xff) + (short) ((bdy[1] & 0xff) << 8));
            short width = (short) ((bwidth[0] & 0xff) + (short) ((bwidth[1] & 0xff) << 8));
            short height = (short) ((bheight[0] & 0xff) + (short) ((bheight[1] & 0xff) << 8));

            // add new screen to master
            if (FullscreenActivity.getInstance() != null) {
                Master master = FullscreenActivity.getInstance().getMaster();
                if (master != null) {
                    synchronized (master) {
                        PhysicalScreen screen = master.getScreen(strOldDeviceIdentifier);
                        if (screen == null) {
                            Log.d(LOG_TAG, "screen is null for id " + strOldDeviceIdentifier);
                            Log.d(LOG_TAG, "Available IDs are:");
                            for (String id : master.getScreenMap().keySet()) {
                                Log.d(LOG_TAG, id);
                            }
                            return;
                        }
                        double x = -dx + screen.getX1();
                        double y = -dy + screen.getY1();
                        master.addSlaveScreen(strNewDeviceIdentifier, new PhysicalScreen(x, y, x + width, y + height));
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(NEW_CONNEXION_PORT);
        } catch (IOException _) {
            return;
        }

        while (true) {
            try {
                Log.d(LOG_TAG, "accepting...");
                Socket socket = serverSocket.accept();
                Thread thread = new Thread(new ActualConnectionListener(socket));
                thread.start();
            } catch (IOException e) {
                Log.d(LOG_TAG, "accepting interrupted (non blocking/error)");
            }
        }
    }
}
