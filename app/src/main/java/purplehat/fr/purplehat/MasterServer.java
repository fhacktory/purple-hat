package purplehat.fr.purplehat;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Created by jmcomets on 11/10/14.
 */
public class MasterServer extends WebSocketServer {
    private static final String LOG_TAG = "MASTER_CLIENT";

    public MasterServer(int port) {
        super(new InetSocketAddress(port));
    }

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
        try {
            JSONObject msg = new JSONObject(message);
            String action = msg.getString("action");
            if (action != null) {
                Log.d(LOG_TAG, "remote '" + conn.getRemoteSocketAddress().getHostName()
                        + "', action: '" + action + "'");

            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "json message not decoded", e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(LOG_TAG, "error", ex);
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text
     *            The String to send across the network.
     * @throws InterruptedException
     *             When socket related I/O errors occur.
     */
    public void broadcast(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}
