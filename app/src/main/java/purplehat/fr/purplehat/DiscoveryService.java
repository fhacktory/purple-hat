package purplehat.fr.purplehat;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;

/**
 * Created by turpif on 11/10/14.
 */
public class DiscoveryService {
    private final static int DISCOVERY_BROADCAST_PORT = 4242;
    private final static int DISCOVERY_HANDSHAKE_PORT = 1337;
    private final static int DISCOVERY_TIMEOUT = 0;
    private BroadcastService broadcastService;
    private Context context;

    public DiscoveryService(Context context) throws IOException {
        this.context = context;
        broadcastService = new BroadcastService(context, DISCOVERY_BROADCAST_PORT, DISCOVERY_TIMEOUT);
    }

    public void waitConnexion(InetAddress masterAddress, int swipeX, int swipeY) throws IOException {
        byte[] sender = new byte[6];
        broadcastService.receive(4, sender);
        InetAddress address = InetAddress.getByAddress(new byte[]{sender[0], sender[1], sender[2], sender[3]});
        byte[] mac = getMACAddress();

        byte[] masterAddrBytes = null;
        if (masterAddress == null) {
            // activateSuperSayanMode(); // TODO devenir un master
            masterAddrBytes = new byte[]{0, 0, 0, 0};
        }
        else {
            masterAddrBytes = masterAddress.getAddress();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException _) {
        }

        Socket socket = new Socket(address, DISCOVERY_HANDSHAKE_PORT);
        socket.getOutputStream().write(masterAddrBytes, 0, 4);
        socket.getOutputStream().write(mac, 0, 6);
        socket.getOutputStream().write(new byte[]{(byte) (swipeX & 0x00FF), (byte) ((swipeX & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (swipeY & 0x00FF), (byte) ((swipeY & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) 0}, 0, 1); // TODO direction
        socket.close();
    }

    private InetAddress getLocalIp() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        return InetAddress.getByAddress(new byte[]{
                (byte) (ip & 0xff),
                (byte) (ip >> 8 & 0xff),
                (byte) (ip >> 16 & 0xff),
                (byte) (ip >> 24 & 0xff)
        });
    }

    private byte[] getMACAddress() {
        // HAAAAACK
        return new byte[] {
                (byte) (Math.random() * 255),
                (byte) (Math.random() * 255),
                (byte) (Math.random() * 255),
                (byte) (Math.random() * 255),
                (byte) (Math.random() * 255),
                (byte) (Math.random() * 255)
        };
    }

    public void askConnexion(int swipeX, int swipeY) throws IOException {
        broadcastService.send(new byte[]{42}, 1);
        ServerSocket serverSocket  = new ServerSocket(DISCOVERY_HANDSHAKE_PORT);
        serverSocket.setSoTimeout(DISCOVERY_TIMEOUT);
        Socket socket = serverSocket.accept();

        byte[] masterAddress = new byte[4];
        byte[] externIdentifier = new byte[6];
        byte[] gesturePosition = new byte[4];
        byte[] gestureDirection = new byte[1];
        socket.getInputStream().read(masterAddress, 0, 4);
        socket.getInputStream().read(externIdentifier, 0, 6);
        socket.getInputStream().read(gesturePosition, 0, 2);
        socket.getInputStream().read(gesturePosition, 2, 2);
        socket.getInputStream().read(gestureDirection, 0, 1);
        socket.close();

        int externX = gesturePosition[0] + (int) gesturePosition[1] << 8;
        int externY = gesturePosition[2] + (int) gesturePosition[3] << 8;
        int dx = swipeX - externX;
        int dy = swipeY - externY;
        byte[] mac = getMACAddress();

        socket = new Socket(InetAddress.getByAddress(masterAddress), ConnexionListener.NEW_CONNEXION_PORT);
        socket.getOutputStream().write(mac, 0, 6);
        socket.getOutputStream().write(externIdentifier, 0, 6);
        socket.getOutputStream().write(new byte[]{(byte) (dx & 0x00FF), (byte) ((dx & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (dy & 0x00FF), (byte) ((dy & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) 0}, 0, 1); // TODO direction
        socket.close();
    }
}
