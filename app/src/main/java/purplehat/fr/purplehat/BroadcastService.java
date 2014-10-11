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

    public BroadcastService(Context context) {
        this.context = context;
    }

    public InetAddress getBroadcastAddress() throws IOException {
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

    public void send(byte[] bytes, int length, int port) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(port);
        datagramSocket.setBroadcast(true);

        InetAddress address = getBroadcastAddress();
        DatagramPacket datagramPacket = new DatagramPacket(bytes, length, address, port);
        datagramSocket.send(datagramPacket);
    }

    public byte[] receive(int length, int port) throws IOException {
        byte[] buf = new byte[1024];
        DatagramSocket datagramSocket = new DatagramSocket(port);
        datagramSocket.setBroadcast(true);

        DatagramPacket packet = new DatagramPacket(buf, length);
        datagramSocket.receive(packet);
        return packet.getData();
    }
}
