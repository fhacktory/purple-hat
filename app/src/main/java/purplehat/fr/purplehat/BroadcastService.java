package purplehat.fr.purplehat;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by turpif on 11/10/14.
 */
public class BroadcastService {
    private Context context;
    private InetAddress broadcastAddress;
    private DatagramSocket socket;
    private int port;

    public  BroadcastService(Context context, int port) throws IOException {
        this.context = context;
        this.port = port;
        this.broadcastAddress = getBroadcastAddress();
        this.socket = new DatagramSocket(this.port);
        this.socket.setBroadcast(true);
    }

    public  BroadcastService(Context context, int port, int timeout) throws IOException {
        this.context = context;
        this.port = port;
        this.broadcastAddress = getBroadcastAddress();
        this.socket = new DatagramSocket(this.port);
        this.socket.setBroadcast(true);
        this.socket.setSoTimeout(timeout);
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo == null) {
            throw new IOException("Pas de DHCP trouvé");
        }

        int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }

        return InetAddress.getByAddress(quads);
    }

    public void send(byte[] bytes, int length) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(bytes, length, broadcastAddress, port);
        socket.send(datagramPacket);
    }

    public byte[] receive(int length) throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, length);
        socket.receive(packet);
        return packet.getData();
    }
}
