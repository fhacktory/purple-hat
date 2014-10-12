package purplehat.fr.purplehat.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import purplehat.fr.purplehat.FullscreenActivity;
import purplehat.fr.purplehat.PhysicalScreen;

/**
 * Created by turpif on 11/10/14.
 */
public class ConnexionListener implements Runnable {
    public final static int NEW_CONNEXION_PORT = 2048;

    private static class NewAbstractConnexionManagerRunnerFactorySingleton implements Runnable {
        Socket socket;

        public NewAbstractConnexionManagerRunnerFactorySingleton(Socket socket) {
            super();
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

            String strNewDeviceIdentifier = null;
            String strOldDeviceIdentifier = null;
            try {
                strNewDeviceIdentifier = new String(newDeviceIdentifier, "UTF-8");
                strOldDeviceIdentifier = new String(oldDeviceIdentifier, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return;
            }
            short dx = (short) ((bdx[0] & 0xff) + (short) ((bdx[1] & 0xff) << 8));
            short dy = (short) ((bdy[0] & 0xff) + (short) ((bdy[1] & 0xff) << 8));
            short width = (short) ((bwidth[0] & 0xff) + (short) ((bwidth[1] & 0xff) << 8));
            short height = (short) ((bheight[0] & 0xff) + (short) ((bheight[1] & 0xff) << 8));
            // TODO direction

            synchronized (FullscreenActivity.getInstance().getMaster()) {
                if (FullscreenActivity.getInstance() != null
                && FullscreenActivity.getInstance().getMaster() != null) {
                    PhysicalScreen screen = FullscreenActivity.getInstance().getMaster().getScreen(strOldDeviceIdentifier);
                    if (screen == null) {
                        return;
                    }
                    double x = -dx + screen.getX1();
                    double y = -dy + screen.getY1();
                    FullscreenActivity.getInstance().getMaster().addSlaveScreen(strNewDeviceIdentifier, new PhysicalScreen(x, y, x + width, y + height));
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
                Socket socket = serverSocket.accept();
                new Thread(new NewAbstractConnexionManagerRunnerFactorySingleton(socket)).start();
            } catch (IOException e) {
                continue;
            }
        }
    }
}
