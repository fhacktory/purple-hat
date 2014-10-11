package purplehat.fr.purplehat;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmcomets on 11/10/14.
 */
public class Master {
    private static final String LOG_TAG = "MASTER_CLIENT";

    private PhysicalScreen baseScreen;
    private Map<String, PhysicalScreen> screenMap;
    private WebSocketServer server;

    public Master(int port, String screenId, PhysicalScreen baseScreen) {
        this.baseScreen = baseScreen;
        screenMap = new HashMap<String, PhysicalScreen>();
        screenMap.put(screenId, baseScreen);
        server = new WebSocketServer(new InetSocketAddress(port)) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                Log.d(LOG_TAG, "connection opened");
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                Log.d(LOG_TAG, "connection closed");
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                Log.e(LOG_TAG, "error", ex);
            }
        };
    }

    public void broadcast(JSONObject obj) {
        broadcast(obj.toString());
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text
     *            The String to send across the network.
     * @throws InterruptedException
     *             When socket related I/O errors occur.
     */
    private void broadcast(String text) {
        Collection<WebSocket> con = server.connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}
