package purplehat.fr.purplehat.network;

import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import purplehat.fr.purplehat.FullscreenActivity;
import purplehat.fr.purplehat.Master;
import purplehat.fr.purplehat.Slave;
import purplehat.fr.purplehat.utils.ScreenUtilitiesService;

/**
 * Created by turpif on 11/10/14.
 */
public class DiscoveryService {
    private final static int DISCOVERY_BROADCAST_PORT = 4242;
    private final static int DISCOVERY_HANDSHAKE_PORT = 1337;
    private final static int DISCOVERY_TIMEOUT = 0;
    private static final String LOG_TAG = "DISCOVERY_SERVICE";
    private BroadcastService broadcastService;
    private Context context;

    public DiscoveryService(Context context) throws IOException {
        this.context = context;
        broadcastService = new BroadcastService(context, DISCOVERY_BROADCAST_PORT, DISCOVERY_TIMEOUT);
    }

    public void waitConnection(InetAddress masterAddress, int swipeX, int swipeY) throws IOException {
        byte[] sender = new byte[6];
        broadcastService.receive(4, sender);
        InetAddress address = InetAddress.getByAddress(new byte[]{sender[0], sender[1], sender[2], sender[3]});

        byte[] masterAddrBytes = null;
        if (masterAddress == null) {
            FullscreenActivity fsa = FullscreenActivity.getInstance();
            if (fsa != null) {
                fsa.becomeAMaster();
            } else {
                Log.w(LOG_TAG, "FSA is null, couldn't become a master");
            }
            masterAddrBytes = getLocalIp().getAddress();
        } else {
            masterAddrBytes = masterAddress.getAddress();
        }

        Master master = FullscreenActivity.getInstance().getMaster();
        Slave slave = FullscreenActivity.getInstance().getSlave();
        byte[] mac = (master != null) ? master.getScreenId().getBytes() : (slave != null ? slave.getId().getBytes() : getMACAddress());
        Log.d(LOG_TAG, "device id: " + new String(mac));

        waitForABit();

        short sswipeX = (short) swipeX;
        short sswipeY = (short) swipeY;
        Socket socket = new Socket(address, DISCOVERY_HANDSHAKE_PORT);
        socket.getOutputStream().write(masterAddrBytes, 0, 4);
        socket.getOutputStream().write(mac, 0, 6);
        socket.getOutputStream().write(new byte[]{(byte) (sswipeX & 0x00FF), (byte) ((sswipeX & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (sswipeY & 0x00FF), (byte) ((sswipeY & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) 0}, 0, 1); // TODO direction
        socket.close();
    }

    public InetAddress getLocalIp() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ip = wifiManager.getConnectionInfo().getIpAddress();
        return InetAddress.getByAddress(new byte[]{
                (byte) (ip & 0xff),
                (byte) (ip >> 8 & 0xff),
                (byte) (ip >> 16 & 0xff),
                (byte) (ip >> 24 & 0xff)
        });
    }

    public void askConnection(int swipeX, int swipeY) throws IOException {
        broadcastService.send(new byte[]{42}, 1);
        ServerSocket serverSocket  = new ServerSocket();
        serverSocket.setSoTimeout(DISCOVERY_TIMEOUT);
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(DISCOVERY_HANDSHAKE_PORT));
        Socket socket = serverSocket.accept();

        byte[] masterAddress = new byte[4];
        byte[] externIdentifier = new byte[6];
        byte[] gesturePositionX = new byte[2];
        byte[] gesturePositionY = new byte[2];
        byte[] gestureDirection = new byte[1];
        socket.getInputStream().read(masterAddress, 0, 4);
        socket.getInputStream().read(externIdentifier, 0, 6);
        socket.getInputStream().read(gesturePositionX, 0, 2);
        socket.getInputStream().read(gesturePositionY, 0, 2);
        socket.getInputStream().read(gestureDirection, 0, 1);
        socket.close();

        short externX = (short) ((byte) (gesturePositionX[0] & 0xff) + (short) ((byte) (gesturePositionX[1] & 0xff) << 8));
        short externY = (short) ((byte) (gesturePositionY[0] & 0xff) + (short) ((byte) (gesturePositionY[1] & 0xff) << 8));
        short dx = (short) (((short) (swipeX)) - externX);
        short dy = (short) (((short) (swipeY)) - externY);
        short width = (short) ScreenUtilitiesService.pixel2mm(new Point(ScreenUtilitiesService.getDisplayCenter().x * 2, 0)).getX().intValue();
        short height = (short) ScreenUtilitiesService.pixel2mm(new Point(0, ScreenUtilitiesService.getDisplayCenter().y * 2)).getY().intValue();
        byte[] mac = getMACAddress();
        waitForABit();

        Log.d(LOG_TAG, "Receive info from " + new String(externIdentifier) + " : dx=" + dx + ", dy=" + dy);

        socket = new Socket(InetAddress.getByAddress(masterAddress), ConnectionListener.NEW_CONNEXION_PORT);
        socket.getOutputStream().write(mac, 0, 6);
        socket.getOutputStream().write(externIdentifier, 0, 6);
        socket.getOutputStream().write(new byte[]{(byte) (dx & 0x00FF), (byte) ((dx & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (dy & 0x00FF), (byte) ((dy & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (width & 0x00FF), (byte) ((width & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) (height & 0x00FF), (byte) ((height & 0xFF00) >> 8)}, 0, 2);
        socket.getOutputStream().write(new byte[]{(byte) 0}, 0, 1); // TODO direction
        socket.close();
        waitForABit();

        // become a slave
        FullscreenActivity fsa = FullscreenActivity.getInstance();
        if (fsa != null) {
            fsa.becomeASlave(masterAddress, new String(mac));
        } else {
            Log.w(LOG_TAG, "FSA is null, couldn't become a slave");
        }
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

    private void waitForABit() {
        waitForABit(500);
    }

    private void waitForABit(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException _) {
        }
    }
}
