package purplehat.fr.purplehat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

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
            byte[] bdirection = new byte[1];

            try {
                socket.getInputStream().read(newDeviceIdentifier, 0, 6);
                socket.getInputStream().read(oldDeviceIdentifier, 0, 6);
                socket.getInputStream().read(bdx, 0, 2);
                socket.getInputStream().read(bdy, 0, 2);
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
            int dx = bdx[0] + bdx[1] << 8;
            int dy = bdx[2] + bdx[3] << 8;
            // TODO direction

            // TODO finir
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
