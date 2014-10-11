package purplehat.fr.purplehat;

import android.content.Context;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by turpif on 11/10/14.
 */
public class DiscoveryService {
    private final static int DISCOVERY_BROADCAST_PORT = 4242;
    private final static int DISCOVERY_HANDSHAKE_PORT = 4242;
    private final static int DISCOVERY_TIMEOUT = 1000;
    private BroadcastService broadcastService;

    public DiscoveryService(Context context) throws IOException {
        broadcastService = new BroadcastService(context, DISCOVERY_BROADCAST_PORT, DISCOVERY_TIMEOUT);
    }

    public void waitConnexion(InetAddress masterAddress, int swipeX, int swipeY) throws IOException {
        byte[] data = broadcastService.receive(4);
        InetAddress address = InetAddress.getByAddress(new byte[]{data[0], data[1], data[2], data[3]});

        InetAddress localHost = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(localHost);
        byte[] mac = network.getHardwareAddress();

        byte[] masterAddrBytes = null;
        if (masterAddress == null) {
            // activateSuperSayanMode(); // TODO devenir un master
            masterAddrBytes = new byte[]{0, 0, 0, 0};
        }
        else {
            masterAddrBytes = masterAddress.getAddress();
        }

        Socket socket = new Socket(address, DISCOVERY_HANDSHAKE_PORT);
        socket.getOutputStream().write(masterAddrBytes, 0, 4);
        socket.getOutputStream().write(mac, 0, 6);
        socket.getOutputStream().write(new byte[]{(byte) (swipeX & 0x00FF), (byte) ((swipeX & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (swipeY & 0x00FF), (byte) ((swipeY & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) 0}); // TODO direction
        socket.close();
    }

    public void askConnexion(int swipeX, int swipeY) throws IOException {
        InetAddress localHost = InetAddress.getLocalHost();
        broadcastService.send(localHost.getAddress(), 4);
        ServerSocket serverSocket  = new ServerSocket(DISCOVERY_HANDSHAKE_PORT);
        serverSocket.setSoTimeout(DISCOVERY_TIMEOUT);
        Socket socket = serverSocket.accept();

        byte[] masterAddress = new byte[4];
        byte[] externIdentifier = new byte[6];
        byte[] gesturePosition = new byte[4];
        socket.getInputStream().read(masterAddress, 0, 4);
        socket.getInputStream().read(externIdentifier, 0, 6);
        socket.getInputStream().read(gesturePosition, 0, 4);
        socket.close();

        int externX = gesturePosition[0] + (int) gesturePosition[1] << 8;
        int externY = gesturePosition[2] + (int) gesturePosition[3] << 8;
        int dx = swipeX - externX;
        int dy = swipeY - externY;

        NetworkInterface network = NetworkInterface.getByInetAddress(localHost);
        byte[] mac = network.getHardwareAddress();

        socket = new Socket(InetAddress.getByAddress(masterAddress), DISCOVERY_HANDSHAKE_PORT);
        socket.getOutputStream().write(mac, 0, 6);
        socket.getOutputStream().write(externIdentifier, 0, 6);
        socket.getOutputStream().write(new byte[]{(byte) (dx & 0x00FF), (byte) ((dx & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (dy & 0x00FF), (byte) ((dy & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) 0}); // TODO direction
        socket.close();
    }
}
